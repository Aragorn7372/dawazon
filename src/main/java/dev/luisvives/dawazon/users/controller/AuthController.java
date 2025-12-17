package dev.luisvives.dawazon.users.controller;
import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.users.dto.UserChangePasswordDto;
import dev.luisvives.dawazon.users.dto.UserRegisterDto;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

        return "web/auth/auth";
    }

    @GetMapping("/auth/signup")
    public String register(Model model) {
        log.info("register");
        return "web/auth/auth";
    }

    @PostMapping("/auth/signup")
    public String register(@Valid @ModelAttribute UserRegisterDto registerDto,
                           BindingResult bindingResult,
                           Model model) {
        log.info("register");
        // Subida de imágenes
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Por favor corrige los errores del formulario");
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "web/auth/auth";
        }
        if (!registerDto.passwordsMatch()) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "web/auth/auth";
        }
        try {
            User usuario = User.builder()
                    .userName(registerDto.getUserName())
                    .email(registerDto.getEmail())
                    .password(registerDto.getPassword())
                    .telefono(registerDto. getTelefono())
                    .build();
            val user = usuarioServicio.register(usuario);
            if (registerDto.getAvatar() != null && !registerDto.getAvatar().isEmpty()) {
                usuarioServicio.updateImage(user.getId(), registerDto.getAvatar());
            }
            cartService.createNewCart(user.getId());
            return "redirect:/auth/signin";
        } catch (Exception e) {
            log.error("Error al registrar usuario", e);
            model.addAttribute("error", "Error al crear la cuenta:  " + e.getMessage());
            return "web/auth/auth";
        }

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

    // COMPRA DE CARRITO =>


}
