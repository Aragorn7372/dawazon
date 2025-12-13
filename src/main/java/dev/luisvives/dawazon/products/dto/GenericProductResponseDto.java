package dev.luisvives.dawazon.products.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO genérico utilizado para representar la respuesta de un producto.
 * <p>
 * Contiene información básica como el identificador, nombre, precio,
 * categoría, descripción e imagen asociada.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>id</b> (<code>Long</code>): Identificador único del elemento.</li>
 *   <li><b>name</b> (<code>String</code>): Nombre del producto o entidad.</li>
 *   <li><b>price</b> (<code>Double</code>): Precio del producto.</li>
 *   <li><b>category</b> (<code>String</code>): Nombre de la categoría asociada.</li>
 *   <li><b>descripcion</b> (<code>String</code>): Descripción del producto o entidad.</li>
 *   <li><b>image</b> (<code>String</code>): Nombre o ruta de la imagen asociada.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericProductResponseDto {

    /**
     * Identificador único del elemento.
     */
    private String id;

    /**
     * Nombre del producto o entidad.
     */
    private String name;

    /**
     * Precio del producto.
     */
    private Double price;

    /**
     * Cantidad de producto en stock
     */
    private Integer stock;

    /**
     * Nombre de la categoría asociada.
     */
    private String category;

    /**
     * Descripción del producto o entidad.
     */
    private String description;
    private List<CommentDto> comments;

    /**
     * Nombre o ruta de la imagen asociada.
     */
    private List<String> image;
}
