package dev.luisvives.dawazon.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

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
    @NotNull
    private String avatar;
}
