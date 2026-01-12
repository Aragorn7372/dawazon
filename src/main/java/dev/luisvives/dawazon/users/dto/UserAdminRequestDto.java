package dev.luisvives.dawazon.users.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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
 * <li><b>calle, ciudad, codigoPostal, provincia</b>: Dirección del usuario</li>
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
    @Pattern(regexp = "^(\\d{9})?$", message = "El teléfono debe tener 9 dígitos o estar vacío")
    private String telefono;
    private Set<String> roles;

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
