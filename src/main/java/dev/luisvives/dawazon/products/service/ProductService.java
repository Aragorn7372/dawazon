package dev.luisvives.dawazon.products.service;

import dev.luisvives.dawazon.common.service.Service;
import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Servicio específico para gestionar productos.
 * <p>
 * Extiende la interfaz genérica {@link Service} y define operaciones adicionales
 * específicas de productos, como filtrado y actualización de imágenes.
 * </p>
 *
 * <p>
 * Tipos genéricos heredados de {@link Service}:
 * <ul>
 *   <li><b>R</b>: {@link GenericProductResponseDto} – tipo de respuesta para get, save, update y patch.</li>
 *   <li><b>ID</b>: <code>Long</code> – tipo del identificador del producto.</li>
 *   <li><b>P</b>: {@link PostProductRequestDto} – DTO para crear o actualizar completamente un producto.</li>
 * </ul>
 * </p>
 */
public interface ProductService extends Service<
        GenericProductResponseDto,
        String,
        PostProductRequestDto
        > {

    /**
     * Obtiene una página de productos aplicando filtros opcionales.
     *
     * @param name     Filtro opcional por nombre del producto.
     * @param maxPrice Filtro opcional por precio máximo.
     * @param category Filtro opcional por categoría.
     * @param pageable Información de paginación y ordenación.
     * @return Página de productos que cumplen los filtros.
     */
    Page<Product> findAll(Optional<String> name,
                          Optional<Double> maxPrice,
                          Optional<String> category,
                          Pageable pageable);

    /**
     * Actualiza la imagen de un producto.
     *
     * @param id    Identificador del producto a actualizar.
     * @param image Imagen enviada en formato MultipartFile.
     * @return DTO con los datos del producto actualizado.
     */
    GenericProductResponseDto updateImage(String id, List<MultipartFile> image);

    /**
     * Actualiza completamente un recurso existente.
     *
     * @param id Identificador del recurso a actualizar.
     * @param entity Objeto completo con los nuevos valores.
     * @return El recurso actualizado.
     */
    GenericProductResponseDto update(String id, PostProductRequestDto entity);
}
