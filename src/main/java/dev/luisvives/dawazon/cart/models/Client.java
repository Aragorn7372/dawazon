package dev.luisvives.dawazon.cart.models;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase embebible que representa la información de un cliente.
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>name</b> (<code>String</code>): Nombre completo del cliente.</li>
 * <li><b>email</b> (<code>String</code>): Correo electrónico.</li>
 * <li><b>phone</b> (<code>String</code>): Número de teléfono.</li>
 * <li><b>address</b> ({@link Address}): Dirección de envío.</li>
 * </ul>
 *
 * @see Cart
 * @see Address
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Client {
    /**
     * Nombre completo del cliente.
     */
    @NotEmpty
    private String name;
    /**
     * Correo electrónico del cliente.
     */
    @Email
    private String email;
    /**
     * Número de teléfono de contacto.
     */
    @NotEmpty
    private String phone;
    /**
     * Dirección de envío del pedido.
     */
    @NotNull
    private Address address;
}
