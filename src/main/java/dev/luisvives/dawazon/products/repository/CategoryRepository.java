package dev.luisvives.dawazon.products.repository;

import dev.luisvives.dawazon.products.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD sobre la entidad {@link Category}.
 * <p>
 * Proporciona métodos para consultar categorías de productos.
 * </p>
 *
 * @see Category
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    /**
     * Obtiene una lista con todos los nombres de categorías.
     * @return Lista de nombres de todas las categorías.
     */
    @Query("SELECT name FROM Category")
    List<String> getAllNames();

    /**
     * Busca una categoría por nombre ignorando mayúsculas/minúsculas.
     * @param name Nombre de la categoría a buscar.
     * @return Optional con la categoría si se encuentra, vacío si no.
     */
    Optional<Category> findByNameIgnoreCase(String name);
}
