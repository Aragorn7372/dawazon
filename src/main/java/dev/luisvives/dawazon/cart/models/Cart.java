package dev.luisvives.dawazon.cart.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa un carrito de compras.
 * <p>
 * Almacenada en MongoDB, gestiona tanto carritos activos como pedidos
 * completados.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>id</b> (<code>ObjectId</code>): Identificador único del carrito.</li>
 * <li><b>userId</b> (<code>Long</code>): ID del usuario propietario del
 * carrito.</li>
 * <li><b>purchased</b> (<code>boolean</code>): Indica si ha sido comprado.</li>
 * <li><b>client</b> ({@link Client}): Información del cliente.</li>
 * <li><b>cartLines</b> ({@link List}<{@link CartLine}>): Productos en el
 * carrito.</li>
 * <li><b>totalItems</b> (<code>Integer</code>): Número total de ítems.</li>
 * <li><b>total</b> (<code>Double</code>): Precio total.</li>
 * <li><b>checkoutInProgress</b> (<code>boolean</code>): Indica si el checkout
 * está en curso.</li>
 * <li><b>checkoutStartedAt</b> ({@link LocalDateTime}): Fecha de inicio del
 * checkout.</li>
 * </ul>
 *
 * @see CartLine
 * @see Client
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
@TypeAlias("Cart")
public class Cart {
    /**
     * Identificador único del carrito en MongoDB.
     */
    @Id
    @Builder.Default
    private ObjectId id = new ObjectId();
    /**
     * ID del usuario propietario del carrito.
     */
    @NotNull
    private Long userId;
    /**
     * Indica si el carrito ha sido comprado.
     */
    @NotNull
    private boolean purchased;
    /**
     * Información del cliente para el envío.
     */
    @NotNull
    private Client client;
    /**
     * Lista de líneas de productos en el carrito.
     */
    @NotNull
    private List<CartLine> cartLines;
    /**
     * Número total de ítems en el carrito.
     */
    @NotNull
    private Integer totalItems;
    /**
     * Precio total del carrito.
     */
    @NotNull
    private Double total;
    /**
     * Fecha y hora de creación del carrito.
     */
    @NotNull
    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    /**
     * Fecha y hora de la última actualización.
     */
    @NotNull
    @LastModifiedDate
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Obtiene el ID del carrito en formato hexadecimal.
     *
     * @return ID como String hexadecimal.
     */
    @JsonProperty("id")
    public String getId() {
        return id.toHexString();
    }

    /**
     * Establece las líneas de carrito y recalcula los totales.
     *
     * @param cartLines Nueva lista de líneas de carrito
     */
    public void setOrderLines(List<CartLine> cartLines) {
        this.cartLines = cartLines;
        this.totalItems = cartLines == null ? 0 : cartLines.size();
        this.total = cartLines == null ? 0.0 : cartLines.stream().mapToDouble(CartLine::getTotalPrice).sum();
    }

    /**
     * Indica si el proceso de checkout está en curso.
     */
    @Builder.Default
    private boolean checkoutInProgress = false;

    /**
     * Fecha y hora de inicio del proceso de checkout.
     */
    private LocalDateTime checkoutStartedAt;

    public long getMinutesSinceCheckoutStarted() {
        if (checkoutStartedAt == null) {
            return 0;
        }
        return java.time.Duration.between(checkoutStartedAt, LocalDateTime.now())
                .toMinutes();
    }

    /**
     * Verifica si el proceso de checkout ha expirado (más de 5 minutos).
     *
     * @return {@code true} si el checkout ha expirado.
     */
    public boolean isCheckoutExpired() {
        return checkoutInProgress &&
                checkoutStartedAt != null &&
                getMinutesSinceCheckoutStarted() > 5;
    }
}
