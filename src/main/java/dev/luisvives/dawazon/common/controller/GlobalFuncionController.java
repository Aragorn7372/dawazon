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

/**
 * Controller Advice global que proporciona atributos de modelo comunes a todas
 * las vistas.
 * <p>
 * Centraliza la lógica para exponer información del usuario autenticado,
 * carrito,
 * categorías, tokens CSRF y utilidades de fecha/hora a todas las plantillas
 * Thymeleaf.
 * </p>
 */
@Slf4j
@ControllerAdvice
public class GlobalFuncionController {
    /**
     * Servicio de productos.
     */
    private ProductService productService;

    /**
     * Servicio de carritos.
     */
    private CartService cartService;

    /**
     * Repositorio de carritos.
     */
    private CartRepository cartRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param productService Servicio de productos
     * @param cartService    Servicio de carritos
     * @param cartRepository Repositorio de carritos
     */
    @Autowired
    public GlobalFuncionController(ProductService productService, CartService cartService,
            CartRepository cartRepository) {
        this.productService = productService;
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    /**
     * Proporciona la lista de categorías disponibles.
     *
     * @return Lista de nombres de categorías
     */
    @ModelAttribute("categorias")
    public List<String> categorias() {
        return productService.getAllCategorias();
    }

    /**
     * Proporciona el usuario actualmente autenticado.
     *
     * @param authentication Información de autenticación
     * @return Usuario autenticado o null si no hay sesión
     */
    @ModelAttribute("currentUser")
    public User getCurrentUser(Authentication authentication) {
        log.info("===== GlobalFuncionController.getCurrentUser() =====");
        log.info("Authentication: " + (authentication != null ? authentication.getClass().getName() : "NULL"));
        log.info("isAuthenticated: " + (authentication != null ? authentication.isAuthenticated() : "N/A"));
        log.info("Principal type: " + (authentication != null && authentication.getPrincipal() != null
                ? authentication.getPrincipal().getClass().getName()
                : "NULL"));

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            log.info("Returning user: ID=" + user.getId() + ", Username=" + user.getUsername());
            return user;
        }
        log.info("Returning NULL user");
        return null;
    }

    /**
     * Indica si existe un usuario autenticado.
     *
     * @param authentication Información de autenticación
     * @return true si hay usuario autenticado
     */
    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Verifica si el usuario actual tiene rol de admin.
     *
     * @param authentication Información de autenticación
     * @return true si el usuario es admin
     */
    @ModelAttribute("isAdmin")
    public Boolean isAdmin(Authentication authentication) {
        log.info("===== isAdmin() CALLED =====");
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            boolean result = user.getRoles() != null && user.getRoles().contains(Role.ADMIN);
            log.info(
                    "isAdmin - User: " + user.getUsername() + ", Roles: " + user.getRoles() + ", Result: " + result);
            return result;
        }
        log.info("isAdmin - No auth, returning false");
        return false;
    }

    /**
     * Verifica si el usuario actual tiene rol de manager.
     *
     * @param authentication Información de autenticación
     * @return true si el usuario es manager
     */
    @ModelAttribute("isManager")
    public Boolean isManager(Authentication authentication) {
        log.info("===== isManager() CALLED =====");
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            boolean result = user.getRoles() != null && user.getRoles().contains(Role.MANAGER);
            log.info(
                    "isManager - User: " + user.getUsername() + ", Roles: " + user.getRoles() + ", Result: " + result);
            return result;
        }
        log.info("isManager - No auth, returning false");
        return false;
    }

    /**
     * Verifica si el usuario actual tiene rol de usuario regular.
     *
     * @param authentication Información de autenticación
     * @return true si el usuario tiene rol USER
     */
    @ModelAttribute("isUser")
    public Boolean isUser(Authentication authentication) {
        log.info("===== isUser() CALLED =====");
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            boolean result = user.getRoles() != null && user.getRoles().contains(Role.USER);
            log.info(
                    "isUser - User: " + user.getUsername() + ", Roles: " + user.getRoles() + ", Result: " + result);
            return result;
        }
        log.info("isUser - No auth, returning false");
        return false;
    }

    /**
     * Proporciona el nombre de usuario del usuario autenticado.
     *
     * @param authentication Información de autenticación
     * @return Nombre de usuario o null
     */
    @ModelAttribute("username")
    public String getUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            return user.getUsername();
        }
        return null;
    }

    /**
     * Proporciona el token CSRF de la petición actual.
     *
     * @param request Petición HTTP
     * @return Token CSRF o cadena vacía
     */
    @ModelAttribute("csrfToken")
    public String getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return csrfToken != null ? csrfToken.getToken() : "";
    }

    /**
     * Proporciona el nombre del parámetro CSRF.
     *
     * @param request Petición HTTP
     * @return Nombre del parámetro CSRF
     */
    @ModelAttribute("csrfParamName")
    public String getCsrfParamName(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return csrfToken != null ? csrfToken.getParameterName() : "_csrf";
    }

    /**
     * Proporciona el nombre del header CSRF.
     *
     * @param request Petición HTTP
     * @return Nombre del header CSRF
     */
    @ModelAttribute("csrfHeaderName")
    public String getCsrfHeaderName(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return csrfToken != null ? csrfToken.getHeaderName() : "X-CSRF-TOKEN";
    }

    /**
     * Proporciona el ID del usuario autenticado.
     *
     * @param authentication Información de autenticación
     * @return ID del usuario o null
     */
    @ModelAttribute("currentUserId")
    public Long getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
        return null;
    }

    /**
     * Proporciona el número de ítems en el carrito.
     *
     * @param request Petición HTTP
     * @return Número de productos en el carrito
     */
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

    /**
     * Indica si el carrito tiene ítems.
     *
     * @param request Petición HTTP
     * @return true si hay ítems en el carrito
     */
    @ModelAttribute("hasCartItems")
    public boolean hasCartItems(HttpServletRequest request) {
        return getCartItemCount(request) > 0;
    }

    /**
     * Proporciona el carrito del usuario actual.
     * <p>
     * Busca primero en sesión, luego en base de datos, y crea uno nuevo si no
     * existe.
     * </p>
     *
     * @param request        Petición HTTP
     * @param authentication Información de autenticación
     * @return Carrito del usuario o carrito vacío
     */
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

    /**
     * Alias de getCarrito para consistencia.
     *
     * @param request        Petición HTTP
     * @param authentication Información de autenticación
     * @return Carrito del usuario
     */
    @ModelAttribute("cart")
    public Cart getCart(HttpServletRequest request, Authentication authentication) {
        // Reutilizar el mismo método para consistencia
        return getCarrito(request, authentication);
    }

    /**
     * Proporciona el número de ítems del carrito como String.
     *
     * @param request Petición HTTP
     * @return Número de ítems como String, o cadena vacía si es 0
     */
    @ModelAttribute("items_carrito")
    public String itemsCarrito(HttpServletRequest request) {
        int count = getCartItemCount(request);
        return count > 0 ? Integer.toString(count) : "";
    }

    /**
     * Proporciona la ruta base para archivos subidos.
     *
     * @return Ruta base "/files/"
     */
    @ModelAttribute("filesPath")
    public String getFilesPath() {
        return "/files/";
    }

    /**
     * Proporciona la fecha y hora actual.
     *
     * @return LocalDateTime actual
     */
    @ModelAttribute("currentDateTime")
    public java.time.LocalDateTime getCurrentDateTime() {
        return java.time.LocalDateTime.now();
    }

    /**
     * Proporciona el año actual.
     *
     * @return Año actual
     */
    @ModelAttribute("currentYear")
    public int getCurrentYear() {
        return java.time.LocalDate.now().getYear();
    }

    /**
     * Proporciona el mes actual en español.
     *
     * @return Nombre del mes en español
     */
    @ModelAttribute("currentMonth")
    public String getCurrentMonth() {
        return java.time.LocalDate.now().getMonth().getDisplayName(
                java.time.format.TextStyle.FULL,
                new java.util.Locale("es", "ES"));
    }

    /**
     * Proporciona el mensaje de error de compra desde la sesión.
     * <p>
     * Implementa patrón flash attribute: se elimina después de mostrarlo.
     * </p>
     *
     * @param request Petición HTTP
     * @return Mensaje de error o null
     */
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
