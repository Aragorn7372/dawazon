package dev.luisvives.dawazon.users.controller;

import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.service.ProductService;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("auth/me")
@PreAuthorize("hasAnyAuthority()")
public class UserController {
    private final AuthService authService;
    private final ProductService productService;

    @Autowired
    public UserController(AuthService authService, ProductService productService) {
        this.authService = authService;
        this.productService = productService;
    }

    @GetMapping({"", "/"})
    public String index(Model model) {
        val user = (User) model.getAttribute("currentUser");
        model.addAttribute("user", user);
        return "/web/user/editUserAdmin";
    }

    @GetMapping("/edit")
    public String edit(Model model) {
        val user = (User) model.getAttribute("currentUser");
        model.addAttribute("user", user);
        return "/web/user/editUserAdmin";
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
        return "/web/productos/productoSaveEdit";
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
            return "/web/productos/productoSaveEdit";
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
        return "/web/productos/productoSaveEdit";
    }
    // esto esta en proceso
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping({"/products/save", "/products/save/"})
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
        val savedProduct = productService.update(product.getId(),product);
        productService.updateOrSaveImage(savedProduct.getId(), file);
        return "redirect:/products/" + savedProduct.getId();
    }
}
