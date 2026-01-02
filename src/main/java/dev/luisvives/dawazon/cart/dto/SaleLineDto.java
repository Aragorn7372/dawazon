package dev.luisvives.dawazon.cart.dto;

import dev.luisvives.dawazon.cart.models.Client;
import dev.luisvives.dawazon.cart.models.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa una línea de venta para administración.
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>saleId</b> (<code>String</code>): ID del carrito/venta.</li>
 * <li><b>productId</b> (<code>String</code>): ID del producto.</li>
 * <li><b>productName</b> (<code>String</code>): Nombre del producto.</li>
 * <li><b>quantity</b> (<code>Integer</code>): Cantidad.</li>
 * <li><b>productPrice</b> (<code>Double</code>): Precio unitario.</li>
 * <li><b>totalPrice</b> (<code>Double</code>): Precio total.</li>
 * <li><b>status</b> ({@link Status}): Estado de la línea.</li>
 * <li><b>managerId</b> (<code>Long</code>): ID del vendedor.</li>
 * <li><b>managerName</b> (<code>String</code>): Nombre del vendedor.</li>
 * <li><b>client</b> ({@link Client}): Información del cliente.</li>
 * <li><b>userId</b> (<code>Long</code>): ID del usuario comprador.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleLineDto {
    private String saleId; // Cart ID
    private String productId;
    private String productName;
    private Integer quantity;
    private Double productPrice;
    private Double totalPrice;
    private Status status;
    private Long managerId;
    private String managerName;

    // Información del cliente
    private Client client;
    private Long userId;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Para facilitar el renderizado
    public String getUserName() {
        return client != null ? client.getName() : "";
    }

    public String getUserEmail() {
        return client != null ? client.getEmail() : "";
    }
}
