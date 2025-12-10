package dev.luisvives.dawazon.cart.repository;

import dev.luisvives.dawazon.cart.models.Cart;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends MongoRepository<Cart, ObjectId> {
}
