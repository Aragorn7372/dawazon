package dev.luisvives.dawazon.products.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO utilizado para crear o actualizar un producto mediante POST o PUT.
 * <p>
 * Todos los campos son obligatorios, salvo la imagen, y deben cumplir con las
 * validaciones
 * especificadas para asegurar la integridad de los datos.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 * <li><b>name</b> (<code>String</code>): Nombre del producto.
 * <ul>
 * <li>No puede estar vacío ni contener solo espacios en blanco.</li>
 * <li>Validado con <code>@NotBlank</code>.</li>
 * </ul>
 * </li>
 * <li><b>price</b> (<code>Double</code>): Precio del producto.
 * <ul>
 * <li>No puede ser nulo (<code>@NotNull</code>).</li>
 * <li>Debe ser mayor o igual a 0.0 (<code>@Min(0)</code>).</li>
 * </ul>
 * </li>
 * <li><b>category</b> (<code>String</code>): Categoría del producto.
 * <ul>
 * <li>No puede estar vacía (<code>@NotBlank</code>).</li>
 * </ul>
 * </li>
 * <li><b>description</b> (<code>String</code>): Descripción del producto.
 * <ul>
 * <li>No puede estar vacía.</li>
 * </ul>
 * </li>
 * <li><b>image</b> (<code>String</code>): Ruta de la imagen del
 * producto.
 * <ul>
 * <li>Opcional: se asigna posteriormente al guardar la imagen en la base de
 * datos.</li>
 * </ul>
 * </li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostProductRequestDto {

    /**
     * Identificador del producto.
     * <p>
     * Se utiliza al actualizar un producto existente. No es necesario al crear uno
     * nuevo.
     * </p>
     */
    private String id;
    /**
     * Nombre del producto.
     * No puede estar vacío ni contener solo espacios en blanco.
     */
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;

    /**
     * Precio del producto.
     * No puede ser nulo y debe ser mayor o igual a 0.0.
     */
    @NotNull(message = "El precio no puede estar vacío")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;

    /**
     * Categoría del producto.
     * No puede estar vacía.
     */
    @NotBlank(message = "La categoría no puede estar vacía")
    private String category;

    /**
     * Descripción del producto.
     */
    private String description;

    /**
     * Ruta de la imagen asociada al producto.
     */
    private List<String> images;

    /**
     * Cantidad en stock del producto.
     * <p>
     * No puede ser negativa. Se valida con {@code @Min(0)}.
     * </p>
     */
    @Min(value = 0, message = "La cantidad no puede ser inferior a 0")
    private Integer stock;

    /**
     * ID del usuario creador del producto.
     * <p>
     * Se utiliza para saber quién creó el producto.
     * </p>
     */
    private Long creatorId;
}
