package dev.luisvives.dawazon.cart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que combina información del cliente y su dirección.
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>name</b> (<code>String</code>): Nombre del cliente.</li>
 * <li><b>email</b> (<code>String</code>): Email del cliente.</li>
 * <li><b>phone</b> (<code>String</code>): Teléfono del cliente.</li>
 * <li><b>number</b> (<code>Short</code>): Número de calle.</li>
 * <li><b>street</b> (<code>String</code>): Calle.</li>
 * <li><b>city</b> (<code>String</code>): Ciudad.</li>
 * <li><b>province</b> (<code>String</code>): Provincia.</li>
 * <li><b>country</b> (<code>String</code>): País.</li>
 * <li><b>postalCode</b> (<code>Integer</code>): Código postal.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientDto {
    @NotEmpty
    private String name;
    @Email
    private String email;
    @NotEmpty
    private String phone;
    @Min(value = 0)
    private Short number;
    @NotEmpty
    private String street;
    @NotEmpty
    private String city;
    @NotEmpty
    private String province;
    @NotEmpty
    private String country;
    @Min(value = 0)
    private Integer postalCode;
}