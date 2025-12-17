package dev.luisvives.dawazon.users.controller;
import dev.luisvives.dawazon.cart.service.CartService;
import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import dev.luisvives.dawazon.common.storage.controller.StorageController;
import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.users.dto.UserChangePasswordDto;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Controller
@Slf4j
public class AuthController {

    final AuthService usuarioServicio;
    final StorageService storageService;
    final CartServiceImpl cartService;

    @Autowired
    public AuthController(AuthService usuarioServicio, StorageService storageService,CartServiceImpl cartService) {
        this.usuarioServicio = usuarioServicio;
        this.storageService = storageService;
        this.cartService = cartService;
    }
    @GetMapping("/auth/signin")
    public String login(Model model) {
        log.info("login");
        // CSRF token is handled by GlobalControllerAdvice
        // Para el formulario de registro
        model.addAttribute("usuario", new User());
        return "web/auth/auth";
    }
    @GetMapping("/auth/signup")
    public String register(Model model) {
        log.info("register");
        return "web/auth/auth";
    }


    @PostMapping("/auth/signup")
    public String register(@ModelAttribute User usuario,
                           @RequestParam("file") MultipartFile file) {
        log.info("register");
        // Subida de imágenes
        if (!file.isEmpty()) {
            String imagen = storageService.store(file);
            usuario.setAvatar(MvcUriComponentsBuilder
                    .fromMethodName(StorageController.class, "serveFile", imagen)
                    .build().toUriString());
        }
        val user=usuarioServicio.register(usuario);
        cartService.createNewCart(user.getId());
        return "redirect:/";
    }
    @PreAuthorize("hasAnyAuthority()")
    @GetMapping("/auth/me/changepassword")
    public String change(Model model){
        log.info("change");
        model.addAttribute("usuario", new User());
        return "web/auth/auth";

    }

    @PreAuthorize("hasAnyAuthority()")
    @PostMapping("/auth/me/changepassword")
    public String changePasswordSubmit(Model model,
            @ModelAttribute("usuario")UserChangePasswordDto usuario,
            @ModelAttribute("currentUserId") Long id
    ){
        log.info("change");
        val user = usuarioServicio.changePassword(usuario, id);
        model.addAttribute("usuario", user);
        return "redirect:/";
    }
}
