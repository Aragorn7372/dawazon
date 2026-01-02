package dev.luisvives.dawazon.cart.models;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase embebible que representa una dirección de envío.
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>number</b> (<code>Short</code>): Número de la calle.</li>
 * <li><b>street</b> (<code>String</code>): Nombre de la calle.</li>
 * <li><b>city</b> (<code>String</code>): Ciudad.</li>
 * <li><b>province</b> (<code>String</code>): Provincia.</li>
 * <li><b>country</b> (<code>String</code>): País.</li>
 * <li><b>postalCode</b> (<code>Integer</code>): Código postal.</li>
 * </ul>
 *
 * @see Client
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Address {
    /**
     * Número de la calle o edificio.
     */
    @Min(value = 0)
    private Short number;
    /**
     * Nombre de la calle.
     */
    @NotEmpty
    private String street;
    /**
     * Ciudad de la dirección.
     */
    @NotEmpty
    private String city;
    /**
     * Provincia o estado.
     */
    @NotEmpty
    private String province;
    /**
     * País de la dirección.
     */
    @NotEmpty
    private String country;
    /**
     * Código postal.
     */
    @Min(value = 0)
    private Integer postalCode;
}
