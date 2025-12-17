package dev.luisvives.dawazon.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartStockRequestDto {
    private ObjectId cartId;
    private String productId;
    private Integer quantity;
}
