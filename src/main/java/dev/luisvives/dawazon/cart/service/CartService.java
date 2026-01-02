package dev.luisvives.dawazon.cart.service;

import dev.luisvives.dawazon.cart.dto.CartStockRequestDto;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.common.service.Service;
import dev.luisvives.dawazon.products.models.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar carritos de compra.
 */
public interface CartService extends Service<Cart, ObjectId, Cart> {
    /**
     * Obtiene carritos con filtros opcionales.
     *
     * @param userId    Filtro opcional por ID de usuario
     * @param purchased Filtro opcional por estado de compra
     * @param pageable  Parámetros de paginación
     * @return Página de carritos
     */
    Page<Cart> findAll(Optional<Long> userId,
            Optional<String> purchased,
            Pageable pageable);

    /**
     * Añade un producto al carrito.
     *
     * @param id        ID del carrito
     * @param productId ID del producto a añadir
     * @return Carrito actualizado
     */
    Cart addProduct(ObjectId id, String productId);

    /**
     * Elimina un producto del carrito.
     *
     * @param id        ID del carrito
     * @param productId ID del producto a eliminar
     * @return Carrito actualizado
     */
    Cart removeProduct(ObjectId id, String productId);

    /**
     * Obtiene múltiples productos por sus IDs.
     *
     * @param productIds Lista de IDs de productos
     * @return Lista de productos
     */
    List<Product> variosPorId(List<String> productIds);

    /**
     * Obtiene el carrito activo de un usuario.
     *
     * @param userId ID del usuario
     * @return Carrito del usuario
     */
    Cart getCartByUserId(Long userId);

    /**
     * Actualiza la cantidad de stock de un producto en el carrito.
     *
     * @param entity DTO con datos de actualización
     * @return Carrito actualizado
     */
    Cart updateStock(CartStockRequestDto entity);

    /**
     * Envía email de confirmación de pedido de forma asíncrona.
     *
     * @param pedido Carrito/pedido completado
     */
    void sendConfirmationEmailAsync(Cart pedido);

    /**
     * Cancela una venta específica.
     *
     * @param ventaId   ID del carrito/venta
     * @param productId ID del producto
     * @param currentId ID del usuario actual
     * @param isAdmin   Si el usuario es administrador
     */
    void cancelSale(String ventaId, String productId, Long currentId, boolean isAdmin);
}
