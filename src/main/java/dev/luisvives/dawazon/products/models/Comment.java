package dev.luisvives.dawazon.products.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * Clase embebible que representa un comentario de un usuario sobre un producto.
 * <p>
 * Los comentarios permiten a los usuarios compartir sus opiniones y
 * experiencias
 * sobre los productos que han adquirido o revisado. Esta clase se embebe dentro
 * de {@link Product} y se almacena en una tabla separada.
 * </p>
 *
 * <p>
 * Características:
 * <ul>
 * <li>Identificación del usuario que realizó el comentario</li>
 * <li>Contenido textual del comentario</li>
 * <li>Indicador de si el comentario está verificado</li>
 * <li>Indicador de si el usuario recomienda el producto</li>
 * <li>Fecha automática de creación</li>
 * </ul>
 * </p>
 *
 * @see Product
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Comment {
    /**
     * ID del usuario que realizó el comentario.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * Contenido textual del comentario.
     */
    @Column(nullable = false)
    private String content;

    /**
     * Indica si el comentario ha sido verificado.
     */
    @Column(nullable = false)
    private boolean verified;

    /**
     * Indica si el usuario recomienda el producto.
     * <p>
     * {@code true} si el usuario recomienda el producto, {@code false} en caso
     * contrario.
     * </p>
     */
    @Column(nullable = false)
    private boolean recommended;

    /**
     * Fecha y hora en que se creó el comentario.
     * <p>
     * Se establece automáticamente al crear el comentario.
     * </p>
     */
    @Column(nullable = false)
    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
