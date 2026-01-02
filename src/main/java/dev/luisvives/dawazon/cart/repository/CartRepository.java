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

/**
 * Repositorio para operaciones con carritos en MongoDB.
 */
@Repository
public interface CartRepository extends MongoRepository<Cart, ObjectId> {
    /**
     * Actualiza el estado de una línea específica del carrito.
     *
     * @param id        ID del carrito
     * @param productId ID del producto en la línea
     * @param status    Nuevo estado
     * @return Número de documentos modificados
     */
    @Query("{ '_id' : ?0, 'cartLines.productId' : ?1 }")
    @Update("{ '$set' : { 'cartLines.$.status' : ?2 } }")
    long updateCartLineStatus(ObjectId id, String productId, Status status);

    /**
     * Busca carritos por ID de usuario con paginación.
     *
     * @param id       ID del usuario
     * @param pageable Parámetros de paginación
     * @return Página de carritos del usuario
     */
    Page<Cart> findByUserId(Long id, Pageable pageable);

    /**
     * Añade una nueva línea al carrito.
     *
     * @param cartId  ID del carrito
     * @param newLine Nueva línea a añadir
     * @return Número de documentos modificados
     */
    @Query("{ '_id' : ?0 }")
    @Update("{ '$push' : { 'cartLines' : ?1 } }")
    long addCartLine(ObjectId cartId, CartLine newLine);

    /**
     * Elimina una línea del carrito.
     *
     * @param cartId       ID del carrito
     * @param lineToRemove Línea a eliminar
     * @return Número de documentos modificados
     */
    @Query("{ '_id' : ?0 }")
    @Update("{ '$pull' : { 'cartLines' : ?1 } }")
    long removeCartLine(ObjectId cartId, CartLine lineToRemove);

    /**
     * Busca un carrito por usuario y estado de compra.
     *
     * @param userId ID del usuario
     * @param b      Estado de compra (true si ya fue comprado)
     * @return Optional con el carrito si existe
     */
    Optional<Cart> findByUserIdAndPurchased(Long userId, boolean b);
}
