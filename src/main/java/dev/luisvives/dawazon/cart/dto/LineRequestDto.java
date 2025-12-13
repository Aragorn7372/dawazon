package dev.luisvives.dawazon.cart.dto;

import dev.luisvives.dawazon.cart.models.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LineRequestDto {
    private ObjectId cartId;
    private String productId;
    private Status status;
}
