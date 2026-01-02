package dev.luisvives.dawazon.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * DTO para actualización de datos de usuario.
 * <p>
 * <b>Campos:</b>
 * <ul>
 * <li><b>nombre</b> (String): Nombre del usuario</li>
 * <li><b>email</b> (String): Email válido</li>
 * <li><b>telefono</b> (String): Teléfono (9 dígitos)</li>
 * <li><b>calle</b> (String): Calle de la dirección</li>
 * <li><b>ciudad</b> (String): Ciudad</li>
 * <li><b>codigoPostal</b> (String): Código postal</li>
 * <li><b>provincia</b> (String): Provincia</li>
 * </ul>
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDto {
    @NotBlank
    @NotNull
    private String nombre;
    @Email
    @NotNull
    private String email;
    @Pattern(regexp = "^\\d{9}$")
    private String telefono;

    // Campos de dirección del cliente
    private String calle;
    private String ciudad;
    private String codigoPostal;
    private String provincia;
}
