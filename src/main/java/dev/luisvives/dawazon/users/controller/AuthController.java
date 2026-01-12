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

/**
 * Controlador de autenticación y registro de usuarios.
 * <p>
 * Gestiona las operaciones de login, registro de nuevos usuarios,
 * y cambio de contraseña. Incluye validación de formularios y
 * manejo de imágenes de avatar.
 * </p>
 */
@Controller
@Slf4j
public class AuthController {

    final AuthService usuarioServicio;
    final StorageService storageService;
    final CartServiceImpl cartService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param usuarioServicio Servicio de usuarios y autenticación.
     * @param storageService  Servicio de almacenamiento de archivos.
     * @param cartService     Servicio de carritos de compra.
     */
    @Autowired
    public AuthController(AuthService usuarioServicio, StorageService storageService,CartServiceImpl cartService) {
        this.usuarioServicio = usuarioServicio;
        this.storageService = storageService;
        this.cartService = cartService;
    }

    /**
     * Muestra la página de inicio de sesión.
     *
     * @param model Modelo de Spring MVC.
     * @return Vista de autenticación.
     */
    @GetMapping("/auth/signin")
    public String login(Model model) {
        log.info("login");
        return "web/auth/auth";
    }

    /**
     * Muestra el formulario de registro de nuevos usuarios.
     *
     * @param model Modelo de Spring MVC.
     * @return Vista de autenticación/registro.
     */
    @GetMapping("/auth/signup")
    public String register(Model model) {
        log.info("register");
        return "web/auth/auth";
    }

    /**
     * Procesa el registro de un nuevo usuario.
     * <p>
     * Válida los datos del formulario, verifica que las contraseñas coincidan,
     * crea el usuario, procesa la imagen de avatar si se proporciona,
     * y crea un carrito vacío para el nuevo usuario.
     * </p>
     *
     * @param registerDto   DTO con datos de registro.
     * @param bindingResult Resultado de validación.
     * @param model         Modelo de Spring MVC.
     * @return Redirección a login si éxito, vista de registro si error.
     */
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
                    .telefono(registerDto.getTelefono())
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

    /**
     * Muestra el formulario de cambio de contraseña.
     * <p>
     * Requiere autenticación previa (cualquier rol).
     * </p>
     *
     * @param model Modelo de Spring MVC.
     * @return Vista de cambio de contraseña.
     */
    @PreAuthorize("hasAnyAuthority()")
    @GetMapping("/auth/me/changepassword")
    public String change(Model model){
        log.info("change");
        model.addAttribute("usuario", new User());
        return "web/auth/auth";

    }

    /**
     * Procesa el cambio de contraseña del usuario autenticado.
     * <p>
     * Requiere autenticación previa y valida la contraseña antigua.
     * </p>
     *
     * @param model   Modelo de Spring MVC.
     * @param usuario DTO con contraseñas (antigua, nueva, confirmación).
     * @param id      ID del usuario actual.
     * @return Redirección a la página principal.
     */
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
