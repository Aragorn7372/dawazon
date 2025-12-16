package dev.luisvives.dawazon.products.repository;

import dev.luisvives.dawazon.products.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findAll(Specification<Product> criterio, Pageable pageable);

    List<Product> findAllBycreatedAtBetween(LocalDateTime ultimaEjecucion, LocalDateTime ahora);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :amount WHERE p.id = :id AND p.stock >= :amount AND p.isDeleted = FALSE AND p.version= :version")
    Optional<Product> substractStock(String id, int amount, Long version);


    List<Product> findAllByCreatorId(Long userId);
    @Modifying
    @Query("UPDATE Product p SET p.isDeleted=true, p.stock=0 WHERE p.id= :id")
    void deleteByIdLogical(String id);
}
