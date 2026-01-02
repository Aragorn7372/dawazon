package dev.luisvives.dawazon.common.exceptions;

import dev.luisvives.dawazon.cart.exceptions.CartException;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.users.exceptions.UserException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Manejador global de excepciones para toda la aplicación.
 * <p>
 * Captura excepciones personalizadas de productos, carritos y usuarios,
 * y las transforma en páginas de error amigables para el usuario.
 * </p>
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Maneja excepciones de producto no encontrado.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(ProductException.NotFoundException.class)
    public String handleProductNotFound(ProductException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Producto no encontrado");
        return "blocked";
    }

    /**
     * Maneja excepciones de validación de productos.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(ProductException.ValidationException.class)
    public String handleValidationException(ProductException ex, Model model) {
        model.addAttribute("error.status", "400");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Error de validación");
        return "blocked";
    }

    /**
     * Maneja excepciones de carrito no encontrado.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(CartException.NotFoundException.class)
    public String handleCartNotFound(CartException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Carrito no encontrado");
        return "blocked";
    }

    /**
     * Maneja excepciones de cantidad de producto excedida.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(CartException.ProductQuantityExceededException.class)
    public String handleProductQuantityExceeded(CartException ex, Model model) {
        model.addAttribute("error.status", "400");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Cantidad excedida");
        return "blocked";
    }

    /**
     * Maneja excepciones de cantidad de intentos excedida.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(CartException.AttemptAmountExceededException.class)
    public String handleAttemptAmountExceeded(CartException ex, Model model) {
        model.addAttribute("error.status", "400");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Cantidad de intentos excedida");
        return "blocked";
    }

    /**
     * Maneja excepciones de acceso no autorizado al carrito.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(CartException.UnauthorizedException.class)
    public String handleCartUnauthorized(CartException ex, Model model) {
        model.addAttribute("error.status", "403");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Acceso denegado");
        return "blocked";
    }

    /**
     * Maneja excepciones de contraseña incorrecta.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(UserException.UserPasswordNotMatchException.class)
    public String handleUserPasswordNotMatchException(UserException ex, Model model) {
        model.addAttribute("error.status", "401");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Contraseña incorrecta");
        return "blocked";
    }

    /**
     * Maneja excepciones de permiso denegado.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(UserException.UserPermissionDeclined.class)
    public String handleUserPermisionDeclined(UserException ex, Model model) {
        model.addAttribute("error.status", "403");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Permiso denegado");
        return "blocked";
    }

    /**
     * Maneja excepciones de producto ya en favoritos.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(UserException.UserHasThatFavProductException.class)
    public String handleUserHasThatFavProductException(UserException ex, Model model) {
        model.addAttribute("error.status", "400");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Producto ya en favoritos");
        return "blocked";
    }

    /**
     * Maneja excepciones genéricas no capturadas por otros handlers.
     *
     * @param ex    Excepción lanzada
     * @param model Modelo para la vista
     * @return Vista de error "blocked"
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.info("Tipo: " + ex.getClass().getName());
        log.info("Mensaje: " + ex.getMessage());
        log.info("Stack trace:");
        ex.printStackTrace();

        model.addAttribute("error.status", "500");
        model.addAttribute("error.message", "Ha ocurrido un error inesperado: " + ex.getMessage());
        model.addAttribute("error.title", "Error interno del servidor");
        return "blocked";
    }

}
