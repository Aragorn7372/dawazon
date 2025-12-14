package dev.luisvives.dawazon.common.exceptions;

import dev.luisvives.dawazon.cart.exceptions.CartException;
import dev.luisvives.dawazon.products.exception.ProductException;
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
        return "/blocked";
    }

    @ExceptionHandler(CartException.NotFoundException.class)
    public String handleNotFound(CartException ex, Model model) {
        model.addAttribute("error.status", "404");
        model.addAttribute("error.message", ex.getMessage());
        model.addAttribute("error.title", "Not Found");
        return "/blocked";
    }
}
