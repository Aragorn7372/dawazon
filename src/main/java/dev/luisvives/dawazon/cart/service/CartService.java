package dev.luisvives.dawazon.cart.service;

import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.common.service.Service;
import dev.luisvives.dawazon.products.models.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CartService extends Service<Cart, ObjectId, Cart> {
    Page<Cart> findAll(Optional<Long> userId,
            Optional<String> purchased,
            Pageable pageable);

    Cart addProduct(ObjectId id, String productId);

    Cart removeProduct(ObjectId id, String productId);

    List<Product> variosPorId(List<String> productIds);
}
