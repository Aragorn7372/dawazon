package dev.luisvives.dawazon.users.controller;

import dev.luisvives.dawazon.cart.dto.CartStockRequestDto;
import dev.luisvives.dawazon.cart.dto.ClientDto;
import dev.luisvives.dawazon.cart.dto.LineRequestDto;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.Client;
import dev.luisvives.dawazon.cart.models.Status;
import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.mapper.ProductMapper;
import dev.luisvives.dawazon.products.service.ProductServiceImpl;
import dev.luisvives.dawazon.users.dto.UserAdminRequestDto;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.exceptions.UserException;
import dev.luisvives.dawazon.users.mapper.UserMapper;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.service.AuthService;
import dev.luisvives.dawazon.users.service.FavService;
import dev.luisvives.dawazon.users.service.UserService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controlador principal del panel de usuario autenticado.
 * <p>
 * Gestiona todas las operaciones del usuario: perfil, productos, favoritos,
 * carrito de compra, pedidos, ventas (para managers), y administración de
 * usuarios (para admins).
 * Requiere autenticación para todos los endpoints (base path: /auth/me).
 * </p>
 */
@Controller
@Slf4j
@RequestMapping("/auth/me")
@PreAuthorize("isAuthenticated()")
public class UserController {
    private final AuthService authService;
    private final ProductServiceImpl productService;
    private final FavService favService;
    private final CartServiceImpl cartService;
    private final UserMapper userMapper;
    private final UserService userService;
    private final ProductMapper mapper;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param authService    Servicio de autenticación y usuarios
     * @param productService Servicio de productos
     * @param favService     Servicio de favoritos
     * @param cartService    Servicio de carritos
     * @param userMapper     Mapper de usuarios
     * @param userService    Servicio de UserDetails
     */
    @Autowired
    public UserController(AuthService authService, ProductServiceImpl productService, FavService favService,
            CartServiceImpl cartService, UserMapper userMapper, UserService userService, ProductMapper mapper) {
        this.authService = authService;
        this.productService = productService;
        this.favService = favService;
        this.cartService = cartService;
        this.userMapper = userMapper;
        this.userService = userService;
        this.mapper = mapper;
    }

    /**
     * Muestra el perfil del usuario autenticado.
     *
     * @param model Modelo de Spring MVC
     * @return Vista del perfil de usuario
     */
    @GetMapping({ "", "/" })
    public String index(Model model) {
        log.info("[GET /auth/me] Cargando perfil de ususario");

        val sessionUser = (User) model.getAttribute("currentUser");
        if (sessionUser == null) {
            log.error("El usuario actual es nulo");
            throw new RuntimeException("El usuario actual es nulo - no autenticado");
        }

        val freshUser = authService.findById(sessionUser.getId());
        if (freshUser == null) {
            throw new RuntimeException("Usuario no encontrado en la bd: ID=" + sessionUser.getId());
        }

        log.info("[GET /auth/me] Usuario cargado: ID={}, Username={}", freshUser.getId(), freshUser.getUsername());
        log.info("[GET /auth/me] Datos de cliente: {}", freshUser.getClient());

        model.addAttribute("user", freshUser);
        return "web/user/userProfile";
    }

    /**
     * Muestra el formulario de edición del perfil de usuario.
     *
     * @param model Modelo de Spring MVC
     * @return Vista de edición de usuario
     */
    @GetMapping("/edit")
    public String edit(Model model) {
        log.info("[GET /auth/me/edit] Cargando formulario de edicion de usuario");
        val user = (User) model.getAttribute("currentUser");
        log.info("[GET /auth/me/edit] Usuario actual: ID={}, Username={}", user.getId(), user.getUsername());
        model.addAttribute("user", user);
        return "web/user/editUserAdmin";
    }

    /**
     * Procesa la actualización del perfil de usuario.
     * <p>
     * Actualiza datos del usuario, procesa imagen de avatar opcional,
     * y actualiza la sesión de seguridad con los nuevos datos.
     * </p>
     *
     * @param updateUser    DTO con datos actualizados
     * @param bindingResult Resultado de validación
     * @param model         Modelo de Spring MVC
     * @param file          Archivo de avatar opcional
     * @return Redirección al perfil, a la vista de edición en caso de error
     */
    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute("user") UserRequestDto updateUser,
            BindingResult bindingResult, Model model,
            @RequestParam(value = "avatar", required = false) MultipartFile file) {
        log.info("[POST /auth/me/edit] Editar request de usuario");
        log.info("[POST /auth/me/edit] UpdateUser data: nombre={}, email={}, telefono={}",
                updateUser.getNombre(), updateUser.getEmail(), updateUser.getTelefono());
        log.info("[POST /auth/me/edit] Archivo de avatar: {}, size: {}",
                file != null && !file.isEmpty(), file != null ? file.getSize() : 0);

        if (bindingResult.hasErrors()) {
            log.error("[POST /auth/me/edit] Errores de validacion: {}", bindingResult.getAllErrors());
            // Volver a mostrar el formulario de edición con los errores
            val currentUser = (User) model.getAttribute("currentUser");
            model.addAttribute("user", currentUser);
            return "web/user/editUserAdmin";
        }

        val id = (Long) model.getAttribute("currentUserId");
        log.info("[POST /auth/me/edit] ID del usuario actual: {}", id);

        log.info("[POST /auth/me/edit] Llamando a authService.updateCurrentUser...");
        val userUpdated = authService.updateCurrentUser(id, updateUser);
        log.info("[POST /auth/me/edit] Usuario actualizado cone exito: ID={}, Username={}",
                userUpdated.getId(), userUpdated.getUsername());

        User finalUser;
        if (file != null && !file.isEmpty()) {
            log.info("[POST /auth/me/edit] Llamando a authService.updateImage...");
            finalUser = authService.updateImage(id, file);
            log.info("[POST /auth/me/edit] Imagen actualizada correctamente, nuevo avatar: {}",
                    finalUser.getAvatar());
        } else {
            log.info("[POST /auth/me/edit] No hay avatar para actualizar");
            finalUser = userUpdated;
        }

        log.info("[POST /auth/me/edit] Actualizando sesion de spring con nuevos datos de usuario");
        val authentication = new UsernamePasswordAuthenticationToken(
                finalUser, finalUser.getPassword(), finalUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("[POST /auth/me/edit] SecurityContext actualizado correctamente");

        log.info("[POST /auth/me/edit] Redirect a /auth/me");
        return "redirect:/auth/me";
    }

    /**
     * Elimina (borrado lógico) la cuenta del usuario actual.
     *
     * @param model Modelo de Spring MVC
     * @return Redirección al logout
     */
    @PostMapping("/delete")
    public String deleteSubmit(Model model) {
        val id = (Long) model.getAttribute("currentUserId");
        authService.softDelete(id);
        return "redirect:/auth/logout";
    }

    /**
     * Muestra formulario para crear nuevo producto (solo MANAGER).
     *
     * @param model Modelo de Spring MVC
     * @return Vista de creación de producto
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping({ "/products/save", "/products/save/" })
    public String save(Model model) {
        return "web/productos/productoSaveEdit";
    }

    /**
     * Obtiene y muestra el listado de productos con filtros y paginación del Manager actual.
     * <p>
     * Endpoint público accesible desde la raíz, /products y /products/.
     * Permite filtrar por nombre y categoría, con soporte de paginación y
     * ordenamiento.
     * </p>
     *
     * @param model     Modelo de Spring MVC para pasar datos a la vista
     * @param page      Número de página (por defecto 0)
     * @param size      Tamaño de página (por defecto 10)
     * @param sortBy    Campo de ordenamiento (por defecto "id")
     * @param direction Dirección de ordenamiento: asc o desc (por defecto "asc")
     * @return Nombre de la vista Thymeleaf "web/productos/lista"
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping({ "/products", "/products/" })
    public String getProducts(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sortBy,
                              @RequestParam(defaultValue = "asc") String direction) {
        val id=(Long)model.getAttribute("currentUserId");
        // Creamos el objeto de ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        val products = mapper.pageToDTO(productService.findAll(Optional.empty(),Optional.empty() ,Optional.of(id), pageable), sortBy, direction);
        model.addAttribute("productos", products);
        return "web/productos/lista";
    }

    /**
     * Guarda un nuevo producto creado por el manager (solo MANAGER).
     *
     * @param product       DTO con datos del producto
     * @param bindingResult Resultado de validación
     * @param model         Modelo de Spring MVC
     * @param file          Lista de archivos de imagen del producto
     * @return Redirección a la página del producto creado
     */
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping({ "/products/save", "/products/save/" })
    public String saveProduct(@Valid @ModelAttribute("producto") PostProductRequestDto product,
            BindingResult bindingResult, Model model,
            @RequestParam("file") List<MultipartFile> file) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("status", 400);
            model.addAttribute("title", "El producto no es válido");
            model.addAttribute("message",
                    bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage));
            return "web/productos/productoSaveEdit";
        }
        product.setCreatorId((Long) model.getAttribute("currentUserId"));
        val savedProduct = productService.save(product);
        productService.updateOrSaveImage(savedProduct.getId(), file);
        return "redirect:/products/" + savedProduct.getId();
    }

    /**
     * Muestra formulario de edición de producto (solo MANAGER).
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del producto
     * @return Vista de edición de producto
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping({ "/products/edit/{id}", "/products/edit/" })
    public String update(Model model, @PathVariable String id) {
        val product = productService.getById(id);
        model.addAttribute("product", product);
        return "web/productos/productoSaveEdit";
    }

    /**
     * Actualiza un producto existente del manager (solo MANAGER).
     * <p>
     * Verifica que el usuario sea el creador del producto antes de permitir la
     * edición.
     * </p>
     *
     * @param product       DTO con datos actualizados
     * @param bindingResult Resultado de validación
     * @param model         Modelo de Spring MVC
     * @param file          Lista de archivos de imagen
     * @return Redirección a la página del producto
     * @throws UserException.UserPermissionDeclined Si el usuario no es el creador
     */
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping({ "/products/edit", "/products/edit/" })
    public String updateProduct(@Valid @ModelAttribute("producto") PostProductRequestDto product,
            BindingResult bindingResult, Model model,
            @RequestParam("file") List<MultipartFile> file) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("status", 400);
            model.addAttribute("title", "El producto no es válido");
            model.addAttribute("message",
                    bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage));
            return "/web/productos/productoSaveEdit";
        }
        product.setCreatorId((Long) model.getAttribute("currentUserId"));
        val userId = productService.getUserProductId(product.getId());
        if (userId != product.getCreatorId()) {
            throw new UserException.UserPermissionDeclined("No puedes editar el producto de otro usuario");
        }
        val savedProduct = productService.update(product.getId(), product);
        productService.updateOrSaveImage(savedProduct.getId(), file);
        return "redirect:/products/" + savedProduct.getId();
    }

    /**
     * Elimina un producto del manager (solo MANAGER).
     * <p>
     * Verifica que el usuario sea el creador del producto antes de permitir la
     * eliminación.
     * </p>
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del producto
     * @return Redirección a la página del producto
     * @throws UserException.UserPermissionDeclined Si el usuario no es el creador
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/products/delete/{id}")
    public String delete(Model model, @PathVariable String id) {
        log.info("");
        val product = productService.getById(id);
        val productId = model.getAttribute("currentUserId");
        val userId = productService.getUserProductId(product.getId());
        if (userId != productId) {
            throw new UserException.UserPermissionDeclined("No puedes eliminar el producto de otro usuario");
        }
        productService.deleteById(id);
        return "redirect:/products/" + id;
    }


    /**
     * Añade un producto a los favoritos del usuario (solo USER).
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del producto
     * @return Redirección a la página del producto
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/fav/add/{id}")
    public String addFav(Model model, @PathVariable String id) {
        val userId = (Long) model.getAttribute("currentUserId");
        favService.addFav(id, userId);
        return "redirect:/products/" + id;
    }

    /**
     * Elimina un producto de los favoritos del usuario (solo USER).
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del producto
     * @return Redirección a la página del producto
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/fav/remove/{id}")
    public String removeFav(Model model, @PathVariable String id) {
        val userId = (Long) model.getAttribute("currentUserId");
        favService.addFav(id, userId);
        return "redirect:/products/" + id;
    }

    /**
     * Lista productos favoritos del usuario con paginación (solo USER).
     *
     * @param model     Modelo de Spring MVC
     * @param page      Número de página
     * @param size      Tamaño de página
     * @param sortBy    Campo de ordenación
     * @param direction Dirección de ordenación
     * @return Vista de lista de productos favoritos
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/fav")
    public String fav(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("");
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        val userId = (Long) model.getAttribute("currentUserId");
        model.addAttribute("productos", favService.getFavs(userId, pageable));
        return "web/productos/lista";
    }

    /**
     * Añade un producto al carrito del usuario (solo USER).
     *
     * @param model   Modelo de Spring MVC
     * @param id      ID del producto
     * @param request Request HTTP para actualizar sesión
     * @return Redirección a la página del producto
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/carrito/add/{id}")
    public String addToCart(Model model, @PathVariable String id, HttpServletRequest request) {
        val userId = (Long) model.getAttribute("currentUserId");
        val cart = (Cart) model.getAttribute("cart");
        val ultimateCart = cartService.addProduct(new ObjectId(cart.getId()), id);

        // Actualizar la sesión con el carrito modificado
        HttpSession session = request.getSession();
        session.setAttribute("cart", ultimateCart);
        session.setAttribute("carrito", ultimateCart);

        model.addAttribute("carrito", ultimateCart);
        return "redirect:/products/" + id;
    }

    /**
     * Elimina un producto del carrito del usuario (solo USER).
     *
     * @param model   Modelo de Spring MVC
     * @param id      ID del producto
     * @param request Request HTTP para actualizar sesión
     * @return Redirección a la página del producto
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/carrito/remove/{id}")
    public String removeToCart(Model model, @PathVariable String id, HttpServletRequest request) {
        val cart = (Cart) model.getAttribute("cart");
        val ultimateCart = cartService.removeProduct(new ObjectId(cart.getId()), id);

        // Actualizar la sesión con el carrito modificado
        HttpSession session = request.getSession();
        session.setAttribute("cart", ultimateCart);
        session.setAttribute("carrito", ultimateCart);

        model.addAttribute("carrito", ultimateCart);
        return "redirect:/products/" + id;
    }

    /**
     * Muestra el carrito del usuario (solo USER).
     *
     * @param model Modelo de Spring MVC
     * @return Vista del carrito
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cart")
    public String getCart(Model model) {
        return "web/cart/cart";
    }

    /**
     * Actualiza la cantidad de un producto en el carrito (solo USER).
     *
     * @param line          DTO con datos de la línea del carrito
     * @param bindingResult Resultado de validación
     * @param model         Modelo de Spring MVC
     * @return Redirección al carrito
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("cart/stock")
    public String updateCartStock(@Valid @ModelAttribute("line") CartStockRequestDto line, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("status", 400);
            model.addAttribute("title", "El producto no es válido");
            model.addAttribute("message",
                    bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage));
            return "web/cart/cart";
        }

        Cart cart = cartService.updateStock(line);
        model.addAttribute("carrito", cart);
        return "redirect:/auth/me/cart";
    }

    /**
     * Lista pedidos del usuario con paginación (solo USER).
     *
     * @param model     Modelo de Spring MVC
     * @param page      Número de página
     * @param size      Tamaño de página
     * @param sortBy    Campo de ordenación
     * @param direction Dirección de ordenación
     * @return Vista de pedidos del usuario
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping({ "/pedidos", "/pedidos/" })
    public String getPedidos(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Long userId = (Long) model.getAttribute("currentUserId");
        val pedidos = cartService.findAll(Optional.of(userId), Optional.of("true"), pageable);
        model.addAttribute("pedidos", pedidos);
        return "web/cart/myOrders";
    }

    /**
     * Muestra detalle de un pedido específico del usuario (solo USER).
     * <p>
     * Verifica que el pedido pertenezca al usuario antes de mostrar los detalles.
     * </p>
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del pedido
     * @return Vista de detalle del pedido
     * @throws UserException.UserPermissionDeclined Si el pedido no pertenece al
     *                                              usuario
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping({ "/pedidos/{id}", "/pedidos/{id}/" })
    public String getOrderDetail(Model model,
            @PathVariable String id) {
        val existingCart = cartService.getById(new ObjectId(id));
        val userId = (Long) model.getAttribute("currentUserId");
        if (!userId.equals(existingCart.getUserId())) {
            throw new UserException.UserPermissionDeclined("El usuario con ID: " + userId
                    + " ha intentado acceder al carrito del usuario con ID: " + existingCart.getUserId());
        }
        model.addAttribute("order", existingCart);
        return "web/cart/orderDetail";
    }

    /**
     * Lista ventas del manager con paginación y cálculo de ganancias (solo
     * MANAGER).
     *
     * @param model     Modelo de Spring MVC
     * @param status    Filtro opcional de estado
     * @param page      Número de página
     * @param size      Tamaño de página
     * @param sortBy    Campo de ordenación
     * @param direction Dirección de ordenación
     * @return Vista de ventas del manager
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping({ "/ventas", "/ventas/" })
    public String getVentas(Model model,
            @RequestParam(required = false) Optional<String> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        val userId = (Long) model.getAttribute("currentUserId");

        val ventas = cartService.findAllSalesAsLines(Optional.of(userId), false, pageable);
        val ganancias = cartService.calculateTotalEarnings(Optional.of(userId), false);

        model.addAttribute("ventas", ventas);
        model.addAttribute("ganancias", ganancias);
        model.addAttribute("currentStatus", status.orElse(""));

        return "web/cart/ventas";
    }

    /**
     * Muestra detalle de una venta específica del manager (solo MANAGER).
     *
     * @param model     Modelo de Spring MVC
     * @param cartId    ID del carrito
     * @param productId ID del producto
     * @return Vista de detalle de venta
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/ventas/{cartId}/{productId}")
    public String getVentas(Model model, @PathVariable String cartId, @PathVariable String productId) {
        val userId = (Long) model.getAttribute("currentUserId");
        val line = cartService.getSaleLineByIds(cartId, productId, userId, false);
        model.addAttribute("venta", line);
        return "web/cart/ventas-detalle";
    }

    /**
     * Muestra formulario de edición de una venta del manager (solo MANAGER).
     *
     * @param model     Modelo de Spring MVC
     * @param cartId    ID del carrito
     * @param productId ID del producto
     * @return Vista de edición de venta
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/ventas/edit/{cartId}/{productId}")
    public String getVentaEdit(Model model, @PathVariable String cartId, @PathVariable String productId) {
        val userId = (Long) model.getAttribute("currentUserId");
        val line = cartService.getSaleLineByIds(cartId, productId, userId, false);
        model.addAttribute("venta", line);
        return "web/cart/ventas-edit";
    }

    /**
     * Procesa la edición de una línea de venta (solo MANAGER).
     *
     * @param model          Modelo de Spring MVC
     * @param lineRequestDto DTO con datos de la línea
     * @return Redirección al detalle de la venta
     */
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/venta/edit")
    public String postVentaEdit(Model model, @Valid @ModelAttribute("line") LineRequestDto lineRequestDto) {
        val userId = (Long) model.getAttribute("currentUserId");
        val line = cartService.update(lineRequestDto);
        model.addAttribute("venta", line);
        return "redirect:auth/me/ventas/" + lineRequestDto.getCartId() + "/" + lineRequestDto.getProductId();
    }

    /**
     * Cancela una venta específica del manager (solo MANAGER).
     *
     * @param model     Modelo de Spring MVC
     * @param cartId    ID del carrito
     * @param productId ID del producto
     * @return Redirección a la lista de ventas
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/ventas/cancel/{cartId}/{productId}")
    public String postVentaCancel(Model model, @PathVariable String cartId, @PathVariable String productId) {
        val userId = (Long) model.getAttribute("currentUserId");
        val line = cartService.getSaleLineByIds(cartId, productId, userId, false);
        LineRequestDto lineRequestDto = LineRequestDto.builder()
                .cartId(new ObjectId(cartId)).productId(productId).status(Status.CANCELADO).build();
        val lineFinal = cartService.update(lineRequestDto);
        model.addAttribute("venta", lineFinal);
        return "redirect:auth/me/ventas";
    }

    /**
     * Lista usuarios con paginación y filtro opcional (solo ADMIN).
     *
     * @param model           Modelo de Spring MVC
     * @param userNameOrEmail Filtro opcional por nombre o email
     * @param page            Número de página
     * @param size            Tamaño de página
     * @param sortBy          Campo de ordenación
     * @param direction       Dirección de ordenación
     * @return Vista de lista de usuarios
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public String getUsers(Model model,
            @RequestParam(required = false) Optional<String> userNameOrEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        val users = authService.findAllPaginated(userNameOrEmail, pageable);
        model.addAttribute("users", users);
        return "web/user/users";
    }

    /**
     * Muestra perfil de un usuario específico (solo ADMIN).
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del usuario
     * @return Vista del perfil del usuario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public String getUsers(Model model, @PathVariable Long id) {
        val users = authService.findById(id);
        model.addAttribute("user", users);
        return "web/user/userProfile";
    }

    /**
     * Muestra formulario de edición de usuario para admin (solo ADMIN).
     *
     * @param model          Modelo de Spring MVC
     * @param id             ID del usuario
     * @param authentication Datos de autenticación
     * @return Vista de edición de usuario admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/edit/{id}")
    public String getEditUsers(Model model, @PathVariable Long id, Authentication authentication) {
        log.info("[GET /auth/me/users/edit/{}] Iniciando edición de usuario", id);

        val users = authService.findById(id);
        log.info("[GET /auth/me/users/edit/{}] Usuario encontrado: ID={}, Username={}, Email={}",
                id, users.getId(), users.getUsername(), users.getEmail());

        if (users.getClient() != null) {
            log.info("[GET /auth/me/users/edit/{}] Client: name={}, email={}",
                    id, users.getClient().getName(), users.getClient().getEmail());
            if (users.getClient().getAddress() != null) {
                log.info("[GET /auth/me/users/edit/{}] Address: street={}, city={}",
                        id, users.getClient().getAddress().getStreet(), users.getClient().getAddress().getCity());
            } else {
                log.warn("[GET /auth/me/users/edit/{}] Address is NULL", id);
            }
        } else {
            log.warn("[GET /auth/me/users/edit/{}] Client is NULL", id);
        }

        if (authentication != null) {
            val currentUser = (User) authentication.getPrincipal();
            log.info("[GET /auth/me/users/edit/{}] CurrentUser: ID={}, Username={}",
                    id, currentUser.getId(), currentUser.getUsername());
        } else {
            log.warn("[GET /auth/me/users/edit/{}] Authentication is NULL!", id);
        }

        model.addAttribute("user", users);
        log.info("[GET /auth/me/users/edit/{}] Model attributes set, returning view", id);
        return "web/user/editUserAdmin";
    }

    /**
     * Procesa la edición de usuario por admin (solo ADMIN).
     *
     * @param model         Modelo de Spring MVC
     * @param user          DTO con datos de usuario (incluye roles)
     * @param bindingResult Resultado de validación
     * @return Redirección a lista de usuarios
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/edit/{id}")
    public String getEditUsers(Model model, @Valid @ModelAttribute("user") UserAdminRequestDto user,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("status", 400);
            model.addAttribute("title", "El producto no es válido");
            model.addAttribute("message",
                    bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage));
            return "web/user/editUserAdmin";
        }
        val userEdit = authService.updateAdminCurrentUser(user.getId(), user);
        model.addAttribute("user", userEdit);
        return "redirect:web/user/users";
    }

    /**
     * Banea (elimina lógicamente) un usuario (solo ADMIN).
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del usuario
     * @return Redirección a lista de usuarios
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/ban/{id}")
    public String banUser(Model model, @PathVariable Long id) {
        authService.softDelete(id);
        return "redirect:/auth/me/users";
    }

    /**
     * Muestra datos de cliente del usuario actual.
     *
     * @param model Modelo de Spring MVC
     * @return Vista de datos de cliente
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @GetMapping("/client")
    public String getClients(Model model) {
        val userId = (Long) model.getAttribute("currentUserId");
        val client = authService.findById(userId).getClient();
        model.addAttribute("client", client);
        return "web/user/clients";
    }

    /**
     * Actualiza los datos de cliente del usuario actual.
     *
     * @param model     Modelo de Spring MVC
     * @param clientDto DTO con datos del cliente
     * @return Redirección al perfil
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @PostMapping("/client/")
    public String editClient(Model model, @Valid @ModelAttribute("client") ClientDto clientDto) {
        Client client = userMapper.toClient(clientDto);
        val currentUserId = (Long) model.getAttribute("currentUserId");
        val existing = authService.findById(currentUserId);
        existing.setClient(client);
        return "redirect:/auth/me/";
    }

    /**
     * Muestra página de checkout con validación de datos de cliente (solo USER).
     *
     * @param model              Modelo de Spring MVC
     * @param redirectAttributes Atributos de redirección
     * @return Vista de checkout si datos válidos, redirección si faltan datos
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cart/checkout")
    public String checkout(Model model, RedirectAttributes redirectAttributes) {
        val user = (User) model.getAttribute("currentUser");
        val cart = (Cart) model.getAttribute("cart");
        if (cart.getTotal() <= 0) {
            return "redirect:/";
        }
        if (user.getClient() == null) {
            redirectAttributes.addFlashAttribute("status", 301);
            redirectAttributes.addFlashAttribute("title", "faltan los datos de cliente");
            redirectAttributes.addFlashAttribute("message", "introduce tus datos");
            return "redirect:/auth/me/client";
        }
        model.addAttribute("client", user.getClient());
        return "web/user/checkout";
    }

    /**
     * Inicia el proceso de pago con Stripe (solo USER).
     *
     * @param model              Modelo de Spring MVC
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a Stripe para el pago
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cart/checkout/pay")
    public String checkoutPay(Model model, RedirectAttributes redirectAttributes) {
        val user = (User) model.getAttribute("currentUser");
        val cart = (Cart) model.getAttribute("cart");
        if (cart.getTotal() <= 0) {
            return "redirect:/";
        }
        if (user.getClient() == null) {
            redirectAttributes.addFlashAttribute("status", 301);
            redirectAttributes.addFlashAttribute("title", "faltan los datos de cliente");
            redirectAttributes.addFlashAttribute("message", "introduce tus datos");
            return "redirect:/auth/me/client";
        }
        model.addAttribute("client", user.getClient());
        val stripe = cartService.checkout(new ObjectId(cart.getId()), cart);
        return "redirect:" + stripe;
    }

    /**
     * Procesa checkout exitoso desde Stripe (solo USER).
     * <p>
     * Verifica la sesión, marca el pedido como completado,
     * envía email de confirmación y crea un nuevo carrito para el usuario.
     * </p>
     *
     * @param sessionId          ID de sesión de Stripe
     * @param model              Modelo de Spring MVC
     * @param redirectAttributes Atributos de redirección
     * @return Vista de éxito o redirección si error
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cart/checkout/success")
    public String checkoutSuccess(
            @RequestParam("session_id") String sessionId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            val user = (User) model.getAttribute("currentUser");
            val cart = (Cart) model.getAttribute("cart");

            log.info("Procesando pago exitoso - Usuario: {} - Session: {}",
                    user.getId(), sessionId);

            if (cart.isCheckoutExpired()) {
                log.warn("Checkout expirado ({} minutos) - Carrito: {}",
                        cart.getMinutesSinceCheckoutStarted(), cart.getId());
                redirectAttributes.addFlashAttribute("errorMessage",
                        "La sesión de pago ha expirado. Por favor, intenta de nuevo.");
                return "redirect:/cart";
            }

            // Verificar que el carrito tenga items
            if (cart.getCartLines().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "El carrito está vacío");
                return "redirect:/cart";
            }

            // Marcar el carrito como comprado (save ya resetea los flags de checkout)
            Cart purchasedCart = cartService.save(cart);

            // Enviar email de confirmación de forma asíncrona
            cartService.sendConfirmationEmailAsync(purchasedCart);

            // Pasar datos a la vista
            model.addAttribute("order", purchasedCart);
            model.addAttribute("client", user.getClient());
            model.addAttribute("sessionId", sessionId);

            log.info("Pedido completado - ID: {} - Total: {}€",
                    purchasedCart.getId(), purchasedCart.getTotal());
            cartService.createNewCart(user.getId());
            return "cart/checkout-success";

        } catch (Exception e) {
            log.error("Error procesando checkout success: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error procesando tu pedido:  " + e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Procesa cancelación de checkout desde Stripe (solo USER).
     * <p>
     * Restaura el stock de los productos y mantiene el carrito del usuario.
     * </p>
     *
     * @param sessionId          ID de sesión de Stripe (opcional)
     * @param model              Modelo de Spring MVC
     * @param redirectAttributes Atributos de redirección
     * @return Redirección al carrito con mensaje informativo
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cart/checkout/cancel")
    public String checkoutCancel(
            @RequestParam(value = "session_id", required = false) String sessionId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            val user = (User) model.getAttribute("currentUser");
            val cart = (Cart) model.getAttribute("cart");

            log.warn("Pago cancelado por usuario: {} - Session ID: {}",
                    user.getId(), sessionId);

            cartService.restoreStock(new ObjectId(cart.getId()));

            redirectAttributes.addFlashAttribute("warningMessage",
                    "Pago cancelado.  Tu carrito sigue disponible.");
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Los productos han sido devueltos al stock.");

            log.info("Stock restaurado para carrito: {}", cart.getId());

            return "redirect:/cart";

        } catch (Exception e) {
            log.error("Eror procesando cancelación:  {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al cancelar el pago");
            return "redirect:/cart";
        }
    }

}
