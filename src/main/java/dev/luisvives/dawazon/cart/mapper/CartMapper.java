package dev.luisvives.dawazon.cart.mapper;

import dev.luisvives.dawazon.cart.dto.SaleLineDto;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.users.models.User;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {
    public SaleLineDto cartlineToSaleLineDto(Cart cart, Product product, CartLine line, User manager) {
        return SaleLineDto.builder()
                .saleId(cart.getId())
                .productId(product.getId())
                .productName(product.getName())
                .quantity(line.getQuantity())
                .productPrice(line. getProductPrice())
                .totalPrice(line.getTotalPrice())
                .status(line.getStatus())
                .managerId(product.getCreatorId())
                .managerName(manager.getUsername())
                .client(cart.getClient())
                .userId(cart.getUserId())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    };
}
