package dev.luisvives.dawazon.users.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
