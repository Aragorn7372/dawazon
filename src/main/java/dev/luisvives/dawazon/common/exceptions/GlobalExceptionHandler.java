package dev.luisvives.dawazon.common.exceptions;

import dev.luisvives.dawazon.cart.exceptions.CartException;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.users.exceptions.UserException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ProductException.NotFoundException.class)
    public String handleNotFound(ProductException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }

    @ExceptionHandler(ProductException.ValidationException.class)
    public String handleValidationException(ProductException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }

    @ExceptionHandler(CartException.NotFoundException.class)
    public String handleNotFound(CartException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }

    @ExceptionHandler(CartException.ProductQuantityExceededException.class)
    public String handleProductQuantityExceeded(CartException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }

    @ExceptionHandler(CartException.AttemptAmountExceededException.class)
    public String handleAttemptAmountExceeded(CartException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }

    @ExceptionHandler(CartException.UnauthorizedException.class)
    public String handleUnauthorizedException(CartException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }


    @ExceptionHandler(UserException.UserPasswordNotMatchException.class)
    public String handleUserPasswordNotMatchException(UserException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }

    @ExceptionHandler(UserException.UserPermissionDeclined.class)
    public String handleUserPermisionDeclined(UserException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }

    @ExceptionHandler(UserException.UserHasThatFavProductException.class)
    public String handleUserHasThatFavProductException(UserException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "blocked";
    }




}
