package dev.luisvives.dawazon.cart.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Clase que representa una línea de producto dentro de un carrito.
 * <p>
 * El precio total se calcula automáticamente al modificar la cantidad o el
 * precio unitario.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>quantity</b> (<code>Integer</code>): Cantidad del producto.</li>
 * <li><b>productId</b> (<code>String</code>): ID del producto.</li>
 * <li><b>productPrice</b> (<code>Double</code>): Precio unitario.</li>
 * <li><b>status</b> ({@link Status}): Estado de la línea.</li>
 * <li><b>totalPrice</b> (<code>Double</code>): Precio total (calculado).</li>
 * </ul>
 *
 * @see Cart
 * @see Status
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartLine {
    /**
     * Cantidad del producto en esta línea.
     */
    @Min(value = 0)
    private Integer quantity;
    /**
     * ID del producto.
     */
    @NotNull
    private String productId;
    /**
     * Precio unitario del producto.
     */
    @Min(value = 0)
    private Double productPrice;
    /**
     * Estado actual de esta línea de pedido.
     */
    @NotNull
    private Status status;
    /**
     * Precio total de esta línea (cantidad x precio unitario).
     */
    @NotNull
    @Setter
    @Builder.Default
    private Double totalPrice = 0.0;

    /**
     * Establece la cantidad y recalcula el precio total.
     *
     * @param quantity Nueva cantidad del producto
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
        this.totalPrice = this.quantity * this.productPrice;
    }
}
