package dev.luisvives.dawazon.cart.dto;

import dev.luisvives.dawazon.cart.models.Client;
import dev.luisvives.dawazon.cart.models.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleLineDto {
    private String saleId; // Cart ID
    private String productId;
    private String productName;
    private Integer quantity;
    private Double productPrice;
    private Double totalPrice;
    private Status status;
    private Long managerId;
    private String managerName;

    // Información del cliente
    private Client client;
    private Long userId;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Para facilitar el renderizado
    public String getUserName() {
        return client != null ? client.getName() : "";
    }

    public String getUserEmail() {
        return client != null ? client.getEmail() : "";
    }
}
