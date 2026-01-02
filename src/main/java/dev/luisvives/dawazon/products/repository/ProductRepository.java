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

/**
 * Repositorio para operaciones CRUD sobre la entidad {@link Product}.
 * <p>
 * Extiende {@link JpaRepository} para proporcionar métodos estándar de acceso a
 * datos
 * y define consultas personalizadas para operaciones específicas de productos.
 * </p>
 *
 * @see Product
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    /**
     * Busca productos aplicando especificaciones (filtros) y paginación.
     *
     * @param criterio Especificación JPA con los criterios de búsqueda
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de productos que cumplen el criterio
     */
    Page<Product> findAll(Specification<Product> criterio, Pageable pageable);

    /**
     * Busca productos creados en un rango de fechas.
     * @param ultimaEjecucion Fecha y hora de inicio del rango
     * @param ahora           Fecha y hora de fin del rango
     * @return Lista de productos creados en el rango especificado
     */
    List<Product> findAllBycreatedAtBetween(LocalDateTime ultimaEjecucion, LocalDateTime ahora);

    /**
     * Resta stock de un producto de forma atómica.
     * <p>
     * Actualiza el stock solo si hay cantidad suficiente y el producto no está
     * eliminado.
     * Usa control de concurrencia optimista mediante {@code version}.
     * </p>
     *
     * @param id      ID del producto
     * @param amount  Cantidad a restar del stock
     * @param version Versión actual del producto para control de concurrencia
     * @return Optional con el producto actualizado si la operación fue exitosa
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :amount WHERE p.id = :id AND p.stock >= :amount AND p.isDeleted = FALSE AND p.version= :version")
    Optional<Product> substractStock(String id, int amount, Long version);

    /**
     * Busca productos por ID del creador con paginación.
     * <p>
     * Permite a un vendedor/administrador ver sus propios productos.
     * </p>
     *
     * @param userId   ID del usuario creador
     * @param pageable Configuración de paginación
     * @return Página de productos creados por el usuario
     */
    Page<Product> findAllByCreatorId(Long userId, Pageable pageable);

    /**
     * Elimina lógicamente un producto.
     * <p>
     * Marca el producto como eliminado ({@code isDeleted = true}) y establece su
     * stock a 0,
     * sin eliminarlo físicamente de la base de datos.
     * </p>
     *
     * @param id ID del producto a eliminar
     */
    @Modifying
    @Query("UPDATE Product p SET p.isDeleted=true, p.stock=0 WHERE p.id= :id")
    void deleteByIdLogical(String id);
}
