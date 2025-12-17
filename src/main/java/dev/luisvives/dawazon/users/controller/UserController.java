package dev.luisvives.dawazon.users.controller;

import dev.luisvives.dawazon.cart.dto.CartStockRequestDto;
import dev.luisvives.dawazon.cart.dto.LineRequestDto;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.Status;
import dev.luisvives.dawazon.cart.service.CartService;
import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;

import dev.luisvives.dawazon.products.service.ProductServiceImpl;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.exceptions.UserException;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.service.AuthService;
import dev.luisvives.dawazon.users.service.FavService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequestMapping("/auth/me")
@PreAuthorize("hasAnyAuthority()")
public class UserController {
    private final AuthService authService;
    private final ProductServiceImpl productService;
    private final FavService favService;
    private final CartServiceImpl cartService;

    @Autowired
    public UserController(AuthService authService, ProductServiceImpl productService, FavService favService,CartServiceImpl cartService) {
        this.authService = authService;
        this.productService = productService;
        this.favService = favService;
        this.cartService = cartService;
    }

    @GetMapping({"", "/"})
    public String index(Model model) {
        val user = (User) model.getAttribute("currentUser");
        model.addAttribute("user", user);
        return "web/user/editUserAdmin";
    }

    @GetMapping("/edit")
    public String edit(Model model) {
        val user = (User) model.getAttribute("currentUser");
        model.addAttribute("user", user);
        return "web/user/editUserAdmin";
    }

    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute("user") UserRequestDto updateUser,
                       BindingResult bindingResult, Model model,
                       @RequestParam("avatar")  MultipartFile file) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error.status", 400);
            model.addAttribute("error.title", "Escribiste tus campos mal");
            model.addAttribute("error.message", bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage));
            return "/blocked";
        }
        val id = (Long) model.getAttribute("currentUserId");
        val userUpdated = authService.updateCurrentUser(id, updateUser);
        val updateImages = authService.updateImage(id, file);
        model.addAttribute("user", updateImages);
        return "redirect:/auth/me";
    }

    @GetMapping("/auth/me")
    public String delete(Model model) {
        val user = (User) model.getAttribute("currentUser");
        model.addAttribute("user", user);
        return "auth/me";
    }

    @PostMapping("/delete")
    public String deleteSubmit(Model model) {
        val id = (Long) model.getAttribute("currentUserId");
        authService.softDelete(id);
        return "redirect:/auth/logout";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping({"/products/save", "/products/save/"})
    public String save(Model model) {
        return "web/productos/productoSaveEdit";
    }
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping({"/products", "/products/"})
    public String products(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "id") String sortBy,
                           @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        val user = (User) model.getAttribute("currentUser");
        assert user != null;
        model.addAttribute("productos",productService.findAllByManagerId(user.getId(),pageable));
        return "web/productos/lista";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping({"/products/save", "/products/save/"})
    public String saveProduct(@Valid @ModelAttribute("producto") PostProductRequestDto product,
                              BindingResult bindingResult, Model model,
                              @RequestParam("file") List<MultipartFile> file) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error.status", 400);
            model.addAttribute("error.title", "El producto no es válido");
            model.addAttribute("error.message", bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage));
            return "web/productos/productoSaveEdit";
        }
        product.setCreatorId((Long) model.getAttribute("currentUserId"));
        val savedProduct = productService.save(product);
        productService.updateOrSaveImage(savedProduct.getId(), file);
        return "redirect:/products/" + savedProduct.getId();
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping({"/products/edit/{id}", "/products/edit/"})
    public String update(Model model,@PathVariable String id) {
        val product= productService.getById(id);
        model.addAttribute("product", product);
        return "web/productos/productoSaveEdit";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping({"/products/edit", "/products/edit/"})
    public String updateProduct(@Valid @ModelAttribute("producto") PostProductRequestDto product,
                              BindingResult bindingResult, Model model,
                              @RequestParam("file") List<MultipartFile> file) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error.status", 400);
            model.addAttribute("error.title", "El producto no es válido");
            model.addAttribute("error.message", bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage));
            return "/web/productos/productoSaveEdit";
        }
        product.setCreatorId((Long) model.getAttribute("currentUserId"));
        val userId = productService.getUserProductId(product.getId());
        if (userId != product.getCreatorId()) {
            throw new UserException.UserPermisionDeclined("No puedes editar el producto de otro usuario");
        }
        val savedProduct = productService.update(product.getId(),product);
        productService.updateOrSaveImage(savedProduct.getId(), file);
        return "redirect:/products/" + savedProduct.getId();
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/products/delete/{id}")
    public String delete(Model model, @PathVariable String id) {
        log.info("");
        val product= productService.getById(id);
        val productId=model.getAttribute("currentUserId");
        val userId = productService.getUserProductId(product.getId());
        if(userId!=productId){
            throw new UserException.UserPermisionDeclined("No puedes eliminar el producto de otro usuario");
        }
        productService.deleteById(id);
        return "redirect:/products/" + id;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/fav/add/{id}")
    public String addFav(Model model, @PathVariable String id) {
        val userId=(Long) model.getAttribute("currentUserId");
        favService.addFav(id,userId);
        return "redirect:/products/" + id;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/fav/remove/{id}")
    public String removeFav(Model model, @PathVariable String id) {
        val userId=(Long) model.getAttribute("currentUserId");
        favService.addFav(id,userId);
        return "redirect:/products/" + id;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/fav")
    public String fav(Model model,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(defaultValue = "id") String sortBy,
                      @RequestParam(defaultValue = "asc") String direction) {
        log.info("");
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        val userId=(Long) model.getAttribute("currentUserId");
        model.addAttribute("productos", favService.getFavs(userId,pageable));
        return "web/productos/lista";
    }

    @PreAuthorize("hasrole('USER')")
    @GetMapping("/carrito/add/{id}")
    public String addToCart(Model model, @PathVariable String id) {
        val userId= (Long) model.getAttribute("currentUserId");
        val cart = (Cart) model.getAttribute("cart");
        val ultimateCart=cartService.addProduct(new ObjectId(cart.getId()), id);
        model.addAttribute("carrito",ultimateCart);
        return "redirect:/products/" + id;
    }

    @PreAuthorize("hasrole('USER')")
    @GetMapping("/carrito/remove/{id}")
    public String removeToCart(Model model, @PathVariable String id) {
        val cart = (Cart) model.getAttribute("cart");
        val ultimateCart=cartService.removeProduct(new ObjectId(cart.getId()), id);
        model.addAttribute("carrito",ultimateCart);
        return "redirect:/products/" + id;
    }

    @PreAuthorize("hasrole('USER')")
    @GetMapping("/cart")
    public String getCart(Model model){
        return "web/cart/cart";
    }

    @PreAuthorize(("hasrole('USER')"))
    @PostMapping("cart/stock")
    public String updateCartStock(@Valid @ModelAttribute("line") CartStockRequestDto line, BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("error.status", 400);
            model.addAttribute("error.title", "El producto no es válido");
            model.addAttribute("error.message", bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage));
            return "web/cart/cart";
        }

        Cart cart=cartService.updateStock(line);
        model.addAttribute("carrito",cart);
        return "redirect:/auth/me/cart";
    }

    @PreAuthorize("hasrole('USER')")
    @GetMapping({"/pedidos", "/pedidos/"})
    public String getPedidos(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "id") String sortBy,
                             @RequestParam(defaultValue = "asc") String direction){
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Long userId= (Long) model.getAttribute("currentUserId");
        val pedidos = cartService.findAll(Optional.of(userId), Optional.of("true"), pageable);
        model.addAttribute("pedidos",pedidos);
        return "web/cart/myOrders";
    }

    @PreAuthorize("hasrole('USER')")
    @GetMapping({"/pedidos/{id}", "/pedidos/{id}/"})
    public String getOrderDetail(Model model,
            @PathVariable String id) {
        val existingCart = cartService.getById(new ObjectId(id));
        val userId= (Long) model.getAttribute("currentUserId");
        if(!userId.equals(existingCart.getUserId())) {
            throw new UserException.UserPermisionDeclined("El usuario con ID: " + userId + " ha intentado acceder al carrito del usuario con ID: " +  existingCart.getUserId());
        }
        model.addAttribute("cart",existingCart);
        return "web/cart/orderDetail";
    }

    @PreAuthorize("hasrole('MANAGER')")
    @GetMapping({"/ventas", "/ventas/"})
    public String getVentas(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "id") String sortBy,
                            @RequestParam(defaultValue = "asc") String direction){
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        val userId= (Long) model.getAttribute("currentUserId");
        val venta=cartService.findAllSalesAsLines(Optional.of(userId),false,pageable);
        model.addAttribute("ventas",venta);
        return "web/cart/ventas";
    }

    @PreAuthorize("hasrole('MANAGER')")
    @GetMapping( "/ventas/{cartId}/{productId}")
    public String getVentas(Model model, @PathVariable String cartId, @PathVariable String productId){
        val userId= (Long) model.getAttribute("currentUserId");
        val line= cartService.getSaleLineByIds(cartId,productId,userId,false);
        model.addAttribute("venta",line);
        return "web/cart/ventas";
    }

    @PreAuthorize("hasrole('MANAGER')")
    @GetMapping( "/ventas/edit/{cartId}/{productId}")
    public String getVentaEdit(Model model, @PathVariable String cartId, @PathVariable String productId){
        val userId= (Long) model.getAttribute("currentUserId");
        val line= cartService.getSaleLineByIds(cartId,productId,userId,false);
        model.addAttribute("venta",line);
        return "web/cart/ventas-edit";
    }


    @PreAuthorize("hasrole('MANAGER')")
    @PostMapping("/venta/edit")
    public String postVentaEdit(Model model, @Valid @ModelAttribute("line") LineRequestDto lineRequestDto){
        val userId= (Long) model.getAttribute("currentUserId");
        val line= cartService.update(lineRequestDto);
        model.addAttribute("line",line);
        return "redirect:auth/me/ventas/"+lineRequestDto.getCartId()+"/"+lineRequestDto.getProductId();
    }

    @PreAuthorize("hasrole('MANAGER')")
    @GetMapping("/ventas/cancel/{cartId}/{productId}")
    public String postVentaCancel(Model model, @PathVariable String cartId, @PathVariable String productId){
        val userId= (Long) model.getAttribute("currentUserId");
        val line= cartService.getSaleLineByIds(cartId,productId,userId,false);
        LineRequestDto lineRequestDto= LineRequestDto.builder()
                .cartId(new ObjectId(cartId)).productId(productId).status(Status.CANCELADO).build();
        val lineFinal= cartService.update(lineRequestDto);
        model.addAttribute("line",lineFinal);
        return "redirect:auth/me/ventas";
    }
}