package dev.luisvives.dawazon.products.mapper;

import dev.luisvives.dawazon.common.dto.PageResponseDTO;
import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.products.dto.CommentDto;
import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.models.Comment;
import dev.luisvives.dawazon.products.models.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper para transformar entidades y DTOs relacionados con
 * productos.
 * <p>
 * Este mapper se encarga de convertir entre modelos
 * ({@link Product}) y DTOs.
 * También gestiona la transformación de URLs de imágenes mediante el
 * {@link StorageService}.
 * </p>
 *
 * @see Product
 * @see GenericProductResponseDto
 * @see PostProductRequestDto
 */
@Component
public class ProductMapper {
    /**
     * Servicio de almacenamiento para generar URLs completas de imágenes.
     */
    private final StorageService storageService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param storageService Servicio de almacenamiento de archivos
     */
    @Autowired
    public ProductMapper(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Convierte un modelo {@link Product} a un DTO de respuesta genérica.
     * <p>
     * Este método transforma los nombres de archivos de imágenes a URLs completas
     * utilizando el {@link StorageService}.
     * </p>
     *
     * @param productoFound Producto a convertir
     * @param commentsFound Lista de comentarios ya transformados a DTO
     * @return DTO con la información del producto y URLs completas de imágenes
     */
    public GenericProductResponseDto modelToGenericResponseDTO(Product productoFound, List<CommentDto> commentsFound) {
        List<String> imageUrls = productoFound.getImages().stream()
                .map(storageService::getUrl)
                .toList();

        return GenericProductResponseDto.builder()
                .id(productoFound.getId())
                .name(productoFound.getName())
                .price(productoFound.getPrice())
                .category(productoFound.getCategory().getName())
                .image(imageUrls)
                .stock(productoFound.getStock())
                .comments(commentsFound)
                .description(productoFound.getDescription()).build();
    }

    /**
     * Convierte un DTO de POST/PUT a un modelo {@link Product}.
     * <p>
     * La categoría no se asigna en este método, debe establecerse en el servicio.
     * </p>
     *
     * @param productoDto DTO con los datos del producto
     * @return Producto construido a partir del DTO (sin categoría asignada)
     */
    public Product postPutDTOToModel(PostProductRequestDto productoDto) {
        return Product.builder()
                .id(productoDto.getId())
                .name(productoDto.getName())
                .price(productoDto.getPrice())
                .stock(productoDto.getStock())
                .description(productoDto.getDescription())
                .creatorId(productoDto.getCreatorId())
                .build();
    }

    /**
     * Convierte un {@link Comment} a{@link CommentDto} incluyendo el nombre del
     * usuario.
     * <p>
     * Transforma el ID de usuario en el comentario a un nombre de usuario legible.
     * </p>
     *
     * @param comment  Comentario a convertir
     * @param userName Nombre del usuario que realizó el comentario
     * @return DTO del comentario con nombre de usuario
     */
    public CommentDto commentToCommentDto(Comment comment, String userName) {
        return CommentDto.builder()
                .comment(comment.getContent())
                .userName(userName)
                .verified(comment.isVerified())
                .recommended(comment.isRecommended())
                .build();
    }

    /**
     * Convierte una {@link Page} de productos a un {@link PageResponseDTO}.
     * <p>
     * Incluye metadatos de paginación como número de página, tamaño, total de
     * elementos,
     * y dirección de ordenamiento.
     * </p>
     *
     * @param page      Página de productos
     * @param sortBy    Campo por el que se ordenó
     * @param direction Dirección de ordenamiento (asc/desc)
     * @return DTO de respuesta paginada
     */
    public PageResponseDTO<Product> pageToDTO(Page<Product> page, String sortBy, String direction) {
        return new PageResponseDTO<>(
                page.getContent()
                        .stream()
                        .map(it -> it)
                        .toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber(),
                page.getNumberOfElements(),
                page.isEmpty(),
                page.isFirst(),
                page.isLast(),
                sortBy,
                direction);
    }
}
