package dev.luisvives.dawazon.users.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para gestión administrativa de usuarios.
 * <p>
 * <b>Campos:</b>
 * <ul>
 * <li><b>id</b> (Long): ID del usuario (mínimo 1)</li>
 * <li><b>nombre</b> (String): Nombre del usuario</li>
 * <li><b>email</b> (String): Email válido</li>
 * <li><b>telefono</b> (String): Teléfono</li>
 * <li><b>roles</b> (String): Roles del usuario</li>
 * <li><b>calle, ciudad, codigoPostal, provincia</b>: Direccióndel usuario</li>
 * </ul>
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAdminRequestDto {
    @Min(1)
    @NotNull
    private Long id;
    @NotBlank
    @NotNull
    private String nombre;
    @NotNull
    @Email
    private String email;
    @Pattern(regexp = "")
    private String telefono;
    private String roles;

    // Campos de dirección del cliente
    private String calle;
    private String ciudad;
    private String codigoPostal;
    private String provincia;
}
