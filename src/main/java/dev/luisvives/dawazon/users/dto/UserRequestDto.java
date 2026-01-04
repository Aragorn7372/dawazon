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
    @Pattern(regexp = "^(\\d{9})?$", message = "El teléfono debe tener 9 dígitos o estar vacío")
    private String telefono;

    // Campos de dirección del cliente
    private String calle;
    private String ciudad;
    private String codigoPostal;
    private String provincia;

    /**
     * Setter personalizado para teléfono que limpia el formato.
     * Elimina +34, espacios, guiones y otros caracteres no numéricos.
     */
    public void setTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            this.telefono = "";
            return;
        }

        // Eliminar espacios, guiones, paréntesis, etc.
        String cleaned = telefono.replaceAll("[\\s\\-().]", "");

        // Si empieza con +34, quitarlo
        if (cleaned.startsWith("+34")) {
            cleaned = cleaned.substring(3);
        }
        // Si empieza con 0034, quitarlo
        else if (cleaned.startsWith("0034")) {
            cleaned = cleaned.substring(4);
        }
        // Si empieza con 34 (sin +) y tiene más de 9 dígitos, asumir que es prefijo
        else if (cleaned.startsWith("34") && cleaned.length() > 9) {
            cleaned = cleaned.substring(2);
        }

        this.telefono = cleaned;
    }
}
