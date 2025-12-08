package dev.luisvives.dawazon.cart.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
@TypeAlias("Cart")
public class Cart {
    @Id
    @Builder.Default
    private ObjectId id = new ObjectId();
    @NotNull
    private Long userId;
    @NotNull
    private boolean purchased;
    @NotNull
    private Client client;
    @NotNull
    private List<CartLine> cartLines;
    @NotNull
    private Integer totalItems;
    @NotNull
    private Double total;
    @NotNull
    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @NotNull
    @LastModifiedDate
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @JsonProperty("id")
    public String getId() {
        return id.toHexString();
    }

    // Campos calculados
    public void setOrderLines(List<CartLine> cartLines) {
        this.cartLines = cartLines;
        this.totalItems = cartLines == null ? 0 : cartLines.size();
        this.total = cartLines == null ? 0.0 : cartLines.stream().mapToDouble(CartLine::getTotalPrice).sum();
    }
}
