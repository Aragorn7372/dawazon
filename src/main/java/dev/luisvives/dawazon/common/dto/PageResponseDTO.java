package dev.luisvives.dawazon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO genérico que representa una respuesta paginada de cualquier entidad.
 * <p>
 * Contiene tanto la lista de productos de la página actual como información
 * de paginación, incluyendo número de páginas, tamaño de página, orden, etc.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>content</b> (<code>List&lt;T&gt;</code>): Lista de productos de la página actual.</li>
 *   <li><b>totalPages</b> (<code>int</code>): Número total de páginas disponibles.</li>
 *   <li><b>totalElements</b> (<code>long</code>): Número total de productos.</li>
 *   <li><b>pageSize</b> (<code>int</code>): Cantidad de productos por página.</li>
 *   <li><b>pageNumber</b> (<code>int</code>): Número de la página actual (comenzando en 0).</li>
 *   <li><b>totalPageElements</b> (<code>int</code>): Número de productos en la página actual (puede ser menor que <code>pageSize</code> si es la última página).</li>
 *   <li><b>empty</b> (<code>boolean</code>): Indica si la página está vacía (sin resultados).</li>
 *   <li><b>first</b> (<code>boolean</code>): Indica si esta es la primera página (<code>pageNumber == 0</code>).</li>
 *   <li><b>last</b> (<code>boolean</code>): Indica si esta es la última página (<code>pageNumber == totalPages - 1</code>).</li>
 *   <li><b>sortBy</b> (<code>String</code>): Campo por el que se ordenan los productos (por ejemplo, "id", "nombre" o "precio").</li>
 *   <li><b>direction</b> (<code>String</code>): Dirección del orden: "asc" (ascendente) o "desc" (descendente).</li>
 * </ul>
 *
 * @param <T> Tipo de los productos contenidos en la lista <code>content</code>.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponseDTO<T> {

    /**
     * Lista de productos de la página actual.
     */
    private List<T> content;

    /**
     * Número total de páginas disponibles.
     */
    private int totalPages;

    /**
     * Número total de productos en la consulta.
     */
    private long totalElements;

    /**
     * Número de productos por página.
     */
    private int pageSize;

    /**
     * Número de la página actual (comienza en 0).
     */
    private int pageNumber;

    /**
     * Número de productos en la página actual.
     * Puede ser menor que {@code pageSize} si es la última página.
     */
    private int totalPageElements;

    /**
     * Indica si la página está vacía (sin resultados).
     */
    private boolean empty;

    /**
     * Indica si esta es la primera página.
     */
    private boolean first;

    /**
     * Indica si esta es la última página.
     */
    private boolean last;

    /**
     * Campo por el que se ha ordenado la página de productos.
     */
    private String sortBy;

    /**
     * Dirección del orden: "asc" (ascendente) o "desc" (descendente).
     */
    private String direction;
}
