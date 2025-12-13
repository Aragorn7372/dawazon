package dev.luisvives.dawazon.cart.models;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Client {
    @NotEmpty
    private String name;
    @Email
    private String email;
    @NotEmpty
    private String phone;
    @NotNull
    private Address address;
}
