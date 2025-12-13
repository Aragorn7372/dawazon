package dev.luisvives.dawazon.products.repository;

import dev.luisvives.dawazon.products.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    @Query("SELECT name FROM Category")
    List<String> getAllNames();
    Optional<Category> findByNameIgnoreCase(String name);
}
