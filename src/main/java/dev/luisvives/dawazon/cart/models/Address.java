package dev.luisvives.dawazon.cart.models;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Address {
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
