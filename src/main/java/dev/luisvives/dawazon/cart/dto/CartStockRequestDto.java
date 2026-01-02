package dev.luisvives.dawazon.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * DTO para solicitudes de actualización de stock en el carrito.
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>cartId</b> (<code>ObjectId</code>): ID del carrito.</li>
 * <li><b>productId</b> (<code>String</code>): ID del producto.</li>
 * <li><b>quantity</b> (<code>Integer</code>): Cantidad a actualizar.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartStockRequestDto {
    /**
     * ID del carrito.
     */
    private ObjectId cartId;
    /**
     * ID del producto.
     */
    private String productId;
    /**
     * Cantidad a actualizar.
     */
    private Integer quantity;
}
