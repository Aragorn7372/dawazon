package dev.luisvives.dawazon.cart.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartLine {
    @Min(value = 0)
    private Integer quantity;
    @NotNull
    private String productId;
    @Min(value = 0)
    private Double productPrice;
    @NotNull
    private Status status;
    @NotNull
    @Setter
    @Builder.Default
    private Double totalPrice = 0.0;

    // Campos calculados
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
        this.totalPrice = this.quantity * this.productPrice;
    }
}
