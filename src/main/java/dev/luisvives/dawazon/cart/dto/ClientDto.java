package dev.luisvives.dawazon.cart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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