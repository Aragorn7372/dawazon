package dev.luisvives.dawazon.cart.repository;

import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.cart.models.Status;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, ObjectId> {
    @Query("{ '_id' : ?0, 'cartLines.productId' : ?1 }")
    @Update("{ '$set' : { 'status' : ?2 } }")
    long updateCartLineStatus(ObjectId id,String productId ,Status status);

    Page<Cart> findByUserId(Long id, Pageable pageable);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$push' : { 'cartLines' : ?1 } }")
    long addCartLine(ObjectId cartId, CartLine newLine);

    // ✅ Retorna long (número de documentos actualizados)
    @Query("{ '_id' : ?0 }")
    @Update("{ '$pull' : { 'cartLines' : ?1 } }")
    long removeCartLine(ObjectId cartId, CartLine lineToRemove);

    Optional<Cart> findByUserIdAndPurchased(Long userId, boolean b);
}
