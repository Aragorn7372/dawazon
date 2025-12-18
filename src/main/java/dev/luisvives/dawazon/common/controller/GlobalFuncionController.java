package dev.luisvives.dawazon.common.controller;

import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.repository.CartRepository;
import dev.luisvives.dawazon.cart.service.CartService;
import dev.luisvives.dawazon.products.service.ProductService;
import dev.luisvives.dawazon.users.models.Role;
import dev.luisvives.dawazon.users.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalFuncionController {
    private ProductService productService;
    private CartService cartService;
    private CartRepository cartRepository;

    @Autowired
    public GlobalFuncionController(ProductService productService, CartService cartService,
            CartRepository cartRepository) {
        this.productService = productService;
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    @ModelAttribute("categorias")
    public List<String> categorias() {
        return productService.getAllCategorias();
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(Authentication authentication) {
        System.out.println("===== GlobalFuncionController.getCurrentUser() =====");
        System.out
                .println("Authentication: " + (authentication != null ? authentication.getClass().getName() : "NULL"));
        System.out.println("isAuthenticated: " + (authentication != null ? authentication.isAuthenticated() : "N/A"));
        System.out.println("Principal type: " + (authentication != null && authentication.getPrincipal() != null
                ? authentication.getPrincipal().getClass().getName()
                : "NULL"));

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            System.out.println("Returning user: ID=" + user.getId() + ", Username=" + user.getUsername());
            return user;
        }
        System.out.println("Returning NULL user");
        return null;
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);
    }

    // ⭐ MÉTODO HELPER PARA VERIFICAR ROL ADMIN ⭐
    @ModelAttribute("isAdmin")
    public Boolean isAdmin(Authentication authentication) {
        System.out.println("===== isAdmin() CALLED =====");
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            boolean result = user.getRoles() != null && user.getRoles().contains(Role.ADMIN);
            System.out.println(
                    "isAdmin - User: " + user.getUsername() + ", Roles: " + user.getRoles() + ", Result: " + result);
            return result;
        }
        System.out.println("isAdmin - No auth, returning false");
        return false;
    }

    @ModelAttribute("isManager")
    public Boolean isManager(Authentication authentication) {
        System.out.println("===== isManager() CALLED =====");
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            boolean result = user.getRoles() != null && user.getRoles().contains(Role.MANAGER);
            System.out.println(
                    "isManager - User: " + user.getUsername() + ", Roles: " + user.getRoles() + ", Result: " + result);
            return result;
        }
        System.out.println("isManager - No auth, returning false");
        return false;
    }

    @ModelAttribute("isUser")
    public Boolean isUser(Authentication authentication) {
        System.out.println("===== isUser() CALLED =====");
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            boolean result = user.getRoles() != null && user.getRoles().contains(Role.USER);
            System.out.println(
                    "isUser - User: " + user.getUsername() + ", Roles: " + user.getRoles() + ", Result: " + result);
            return result;
        }
        System.out.println("isUser - No auth, returning false");
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

    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return 0;
        }

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null || cart.getCartLines() == null) {
            return 0;
        }

        return cart.getCartLines().size();
    }

    @ModelAttribute("hasCartItems")
    public boolean hasCartItems(HttpServletRequest request) {
        return getCartItemCount(request) > 0;
    }

    @ModelAttribute("carrito")
    public Cart getCarrito(HttpServletRequest request, Authentication authentication) {
        HttpSession session = request.getSession(false);

        // Si no hay usuario autenticado, devolver carrito vacío
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String) {
            Cart emptyCart = new Cart();
            emptyCart.setCartLines(new ArrayList<>());
            return emptyCart;
        }

        // Si hay sesión, buscar en la sesión primero
        if (session != null) {
            Cart sessionCart = (Cart) session.getAttribute("cart");
            if (sessionCart != null) {
                return sessionCart;
            }
        }

        // Si no hay carrito en sesión, obtenerlo o crearlo para el usuario autenticado
        User user = (User) authentication.getPrincipal();
        Cart cart;

        try {
            // Intentar obtener el carrito existente
            cart = cartService.getCartByUserId(user.getId());
        } catch (Exception e) {
            // Si no existe, crear uno nuevo y guardarlo en MongoDB
            cart = Cart.builder()
                    .userId(user.getId())
                    .cartLines(new ArrayList<>())
                    .build();
            cart = cartRepository.save(cart);
        }

        // Guardar en sesión para futuras peticiones
        if (session != null) {
            session.setAttribute("cart", cart);
        } else {
            // Crear sesión si no existe
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("cart", cart);
        }

        return cart;
    }

    @ModelAttribute("cart")
    public Cart getCart(HttpServletRequest request, Authentication authentication) {
        // Reutilizar el mismo método para consistencia
        return getCarrito(request, authentication);
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
                new java.util.Locale("es", "ES"));
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
