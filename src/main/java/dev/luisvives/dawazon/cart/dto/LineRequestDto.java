package dev.luisvives.dawazon.cart.dto;

import dev.luisvives.dawazon.cart.models.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * DTO para solicitudes de actualización de línea de carrito.
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>cartId</b> (<code>ObjectId</code>): ID del carrito.</li>
 * <li><b>productId</b> (<code>String</code>): ID del producto.</li>
 * <li><b>status</b> ({@link Status}): Nuevo estado de la línea.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LineRequestDto {
    private ObjectId cartId;
    private String productId;
    private Status status;
}
