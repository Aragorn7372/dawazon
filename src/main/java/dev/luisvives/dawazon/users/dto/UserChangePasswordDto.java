package dev.luisvives.dawazon.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el cambio de contraseña de usuario.
 * <p>
 * <b>Campos:</b>
 * <ul>
 * <li><b>oldPassword</b> (String): Contraseña actual</li>
 * <li><b>newPassword</b> (String): Nueva contraseña</li>
 * <li><b>confirmPassword</b> (String): Confirmación de nueva contraseña</li>
 * </ul>
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserChangePasswordDto {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
