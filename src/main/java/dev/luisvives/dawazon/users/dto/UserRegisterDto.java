package dev.luisvives.dawazon.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para el registro de nuevos usuarios.
 * <p>
 * <b>Campos:</b>
 * <ul>
 * <li><b>userName</b> (String): Nombre de usuario único (3-50 caracteres)</li>
 * <li><b>email</b> (String): Email válido y único</li>
 * <li><b>password</b> (String): Contraseña (mínimo 6 caracteres)</li>
 * <li><b>confirmPassword</b> (String): Confirmación de contraseña</li>
 * <li><b>telefono</b> (String): Teléfono opcional</li>
 * <li><b>avatar</b> (MultipartFile): Imagen de perfil opcional</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDto {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String userName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "Debes confirmar la contraseña")
    private String confirmPassword;

    private String telefono;

    private MultipartFile avatar;

    /**
     * Valida que las contraseñas coincidan.
     *
     * @return true si password y confirmPassword son iguales
     */
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}