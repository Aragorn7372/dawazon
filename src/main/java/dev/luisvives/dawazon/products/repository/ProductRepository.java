package dev.luisvives.dawazon.products.repository;

import dev.luisvives.dawazon.products.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findAll(Specification<Product> criterio, Pageable pageable);

    List<Product> findAllByFechaCreacionBetween(LocalDateTime ultimaEjecucion, LocalDateTime ahora);
}
