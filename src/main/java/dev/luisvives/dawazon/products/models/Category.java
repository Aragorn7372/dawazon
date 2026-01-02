package dev.luisvives.dawazon.products.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * Entidad que representa una categoría de productos en el sistema.
 * <p>
 * Las categorías permiten clasificar y organizar los productos en grupos
 * lógicos,
 * facilitando la búsqueda y navegación de los usuarios.
 * </p>
 *
 * <p>
 * Características:
 * <ul>
 * <li>ID generado automáticamente mediante
 * {@link dev.luisvives.dawazon.common.utils.IdGenerator}</li>
 * <li>Nombre único de categoría</li>
 * <li>Auditoría con fechas de creación y actualización</li>
 * </ul>
 * </p>
 *
 * @see Product
 * @see dev.luisvives.dawazon.common.utils.IdGenerator
 */
@Data
@Entity
public class Category {
    /**
     * Identificador único de la categoría.
     * <p>
     * Generado automáticamente mediante
     * {@link dev.luisvives.dawazon.common.utils.IdGenerator}.
     * </p>
     */
    @Id
    @GeneratedValue(generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "dev.luisvives.dawazon.common.utils.IdGenerator")
    private String id;

    /**
     * Nombre de la categoría.
     * <p>
     * Debe ser único en el sistema para evitar duplicados.
     * </p>
     */
    @Column(nullable = false)
    private String name;

    /**
     * Fecha y hora de creación de la categoría.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización de la categoría.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
