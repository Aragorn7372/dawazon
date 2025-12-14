package dev.luisvives.dawazon.common.controller;

import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.products.service.ProductService;
import dev.luisvives.dawazon.users.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalFuncionController {
    private ProductService productService;
    @Autowired
    public GlobalFuncionController(ProductService productService) {
        this.productService = productService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);
    }

    // ⭐ AÑADIR MÉTODO HELPER PARA ADMIN ⭐
    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            return "ADMIN".equals(user.getRoles());
        }
        return false;
    }
     @ModelAttribute("isManager")
    public boolean isManager(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            return "MANAGER".equals(user.getRoles());
        }
        return false;
    }
    @ModelAttribute("isUser")
    public boolean isUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            return "USER".equals(user.getRoles());
        }
        return false;
    }

    @ModelAttribute("username")
    public String getUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            return user.getUsername();
        }
        return null;
    }
    @ModelAttribute("csrfToken")
    public String getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return csrfToken != null ? csrfToken.getToken() : "";
    }

    @ModelAttribute("csrfParamName")
    public String getCsrfParamName(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return csrfToken != null ? csrfToken.getParameterName() : "_csrf";
    }

    @ModelAttribute("csrfHeaderName")
    public String getCsrfHeaderName(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return csrfToken != null ? csrfToken.getHeaderName() : "X-CSRF-TOKEN";
    }
    @ModelAttribute("currentUserId")
    public Long getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
        return null;
    }
    @ModelAttribute("carrito")
    public List<Product> productosCarrito(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        List<Long> contenido = (List<Long>) session.getAttribute("carrito");
        return (contenido == null) ? new ArrayList<>() : productService.variosPorId(contenido);
    }

    // ⭐ SHOPPING CART INFORMATION - FOR ALL PAGES ⭐
    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            List<Long> carrito = (List<Long>) session.getAttribute("carrito");
            return (carrito != null) ? carrito.size() : 0;
        }
        return 0;
    }

    @ModelAttribute("cartTotal")
    public Double getCartTotal(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            List<Long> carrito = (List<Long>) session.getAttribute("carrito");
            if (carrito != null && !carrito.isEmpty()) {
                List<Product> productos = productService.variosPorId(carrito);
                return productos.stream()
                        .mapToDouble(Product::getPrecio)
                        .sum();
            }
        }
        return 0.0;
    }

    @ModelAttribute("hasCartItems")
    public boolean hasCartItems(HttpServletRequest request) {
        return getCartItemCount(request) > 0;
    }

    @ModelAttribute("items_carrito")
    public String itemsCarrito(HttpServletRequest request) {
        int count = getCartItemCount(request);
        return count > 0 ? Integer.toString(count) : "";
    }


    @ModelAttribute("filesPath")
    public String getFilesPath() {
        return "/files/";
    }

    @ModelAttribute("currentDateTime")
    public java.time.LocalDateTime getCurrentDateTime() {
        return java.time.LocalDateTime.now();
    }

    @ModelAttribute("currentYear")
    public int getCurrentYear() {
        return java.time.LocalDate.now().getYear();
    }

    @ModelAttribute("currentMonth")
    public String getCurrentMonth() {
        return java.time.LocalDate.now().getMonth().getDisplayName(
                java.time.format.TextStyle.FULL,
                new java.util.Locale("es", "ES")
        );
    }

    @ModelAttribute("compraError")
    public String getCompraError(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String compraError = (String) session.getAttribute("compra_error");
            // Quitar el error tras mostrarlo (flash attribute)
            if (compraError != null) {
                session.removeAttribute("compra_error");
            }
            return compraError;
        }
        return null;
    }


}
