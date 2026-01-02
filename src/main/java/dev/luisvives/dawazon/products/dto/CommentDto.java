package dev.luisvives.dawazon.products.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferir información de comentarios.
 * @see dev.luisvives.dawazon.products.models.Comment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    /**
     * Nombre de usuario que realizó el comentario.
     */
    private String userName;

    /**
     * Contenido textual del comentario.
     */
    private String comment;

    /**
     * Indica si el usuario recomienda el producto.
     */
    private boolean recommended;

    /**
     * Indica si el comentario ha sido verificado.
     */
    private boolean verified;
}
