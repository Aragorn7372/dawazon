package dev.luisvives.dawazon.products.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.dawazon.common.service.Service;
import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.products.dto.CommentDto;
import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.mapper.ProductMapper;
import dev.luisvives.dawazon.products.models.Category;
import dev.luisvives.dawazon.products.models.Comment;
import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.products.repository.CategoryRepository;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import dev.luisvives.dawazon.products.exception.ProductException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de productos (Producto).
 * <p>
 * Esta capa actúa como puente entre el controlador y los repositorios de
 * productos y categorías.
 * Contiene la lógica de negocio, manejo de cache, validaciones, integridad
 * referencial,
 * y envía notificaciones vía WebSocket cuando hay cambios en los productos.
 * </p>
 *
 * <p>
 * Anotaciones:
 * <ul>
 * <li>{@link Service}: Indica que esta clase es un servicio gestionado por
 * Spring.</li>
 * <li>{@link CacheConfig}: Configura la caché con nombre "productos".</li>
 * </ul>
 * </p>
 *
 * @see ProductService
 * @see ProductMapper
 * @see StorageService
 */
@org.springframework.stereotype.Service
@CacheConfig(cacheNames = { "productos" })
public class ProductServiceImpl implements ProductService {

    private final Logger log = Logger.getLogger(ProductServiceImpl.class.getName());

    /**
     * Repositorio de productos para operaciones CRUD.
     */
    private final ProductRepository repository;

    /**
     * Repositorio de categorías para validaciones de integridad referencial.
     */
    private final CategoryRepository categoryRepository;
    /**
     * Repositorio de categorías para validaciones de integridad referencial.
     */
    private final UserRepository userRepository;
    /**
     * Servicio de almacenamiento para manejar imágenes.
     */
    private final StorageService storageService;

    /**
     * Mapper de Jackson para serializar objetos a JSON.
     */
    ObjectMapper jacksonMapper;
    ProductMapper mapper;

    /**
     * Constructor que inyecta dependencias necesarias.
     *
     * @param repository         Repositorio de productos.
     * @param categoryRepository Repositorio de categorías.
     * @param storageService     Servicio de almacenamiento de imágenes.
     */
    @Autowired
    public ProductServiceImpl(ProductRepository repository,
            CategoryRepository categoryRepository,
            StorageService storageService,
            ProductMapper mapper,
            UserRepository userRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.storageService = storageService;
        this.jacksonMapper = new ObjectMapper();
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    /**
     * Busca productos aplicando filtros opcionales por nombre, precio máximo y
     * categoría.
     *
     * @param name     Filtro opcional por nombre.
     * @param pageable Paginación y ordenación.
     * @return Página de productos que cumplen los filtros.
     */
    @Override
    public Page<Product> findAll(Optional<String> name,
            Optional<String> category,
            Optional<Long> idCreator,
            Pageable pageable) {
        var isDeleted = false;
        Specification<Product> specNameProducto = (root, query, criteriaBuilder) -> name
                .map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + n.toLowerCase() + "%"))
                .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Product> specIsDeleted = (root, query, criteriaBuilder) -> criteriaBuilder
                .equal(root.get("isDeleted"), isDeleted);

        Specification<Product> specCategory = (root, query, criteriaBuilder) -> category
                .map(c -> criteriaBuilder.equal(root.get("category").get("name"), c))
                .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Product> specIdCreator = (root, query, criteriaBuilder) -> idCreator
                .map(id -> criteriaBuilder.equal(root.get("creatorId"), id))
                .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Product> criterio = Specification.allOf(
                specNameProducto,
                specIsDeleted,
                specCategory,
                specIdCreator);

        return repository.findAll(criterio, pageable);
    }

    /**
     * Recupera un producto por su ID.
     * <p>
     * Se utiliza caché para mejorar el rendimiento.
     * </p>
     *
     * @param id Identificador del producto.
     * @return DTO genérico del producto.
     * @throws ProductException.NotFoundException si no existe el producto.
     */
    @Override
    @Cacheable(key = "#id")
    public GenericProductResponseDto getById(String id) {
        log.info("SERVICE: Buscando Producto con id: " + id);

        Product productoFound = repository.findById(id)
                .orElseThrow(() -> {
                    log.warning("SERVICE: No se encontró Producto con id: " + id);
                    return new ProductException.NotFoundException("No se encontró Producto con id: " + id);
                });
        val commentsDto = mapearComentarios(productoFound);

        return mapper.modelToGenericResponseDTO(productoFound, commentsDto);
    }

    public Long getUserProductId(String id) {
        log.info("SERVICE: Buscando Producto con id: " + id);

        Product productoFound = repository.findById(id)
                .orElseThrow(() -> {
                    log.warning("SERVICE: No se encontró Producto con id: " + id);
                    return new ProductException.NotFoundException("No se encontró Producto con id: " + id);
                });

        return productoFound.getCreatorId();
    }

    /**
     * Crea un nuevo producto.
     * <p>
     * Valida integridad referencial con la categoría y envía notificación de
     * creación.
     * </p>
     *
     * @param productoDto DTO con datos del producto.
     * @return DTO genérico del producto creado.
     * @throws ProductException.ValidationException si la categoría no existe.
     */
    @Override
    @Transactional
    public GenericProductResponseDto save(PostProductRequestDto productoDto) {
        log.info("SERVICE: Guardando Producto");

        var existingCategory = categoryRepository.findByNameIgnoreCase(productoDto.getCategory());
        if (existingCategory.isEmpty()) {
            log.warning("SERVICE: Se intentó crear un Producto de una categoría inexistente");
            throw new ProductException.ValidationException("La categoría " + productoDto.getCategory() + " no existe.");
        }

        Product productoModel = mapper.postPutDTOToModel(productoDto);
        productoModel.setCategory(existingCategory.get());
        Product savedProducto = repository.save(productoModel);

        log.info("SERVICE: Producto con id " + savedProducto.getId() + " creado correctamente");
        val commentsDto = mapearComentarios(savedProducto);
        return mapper.modelToGenericResponseDTO(savedProducto, commentsDto);
    }

    /**
     * Actualiza completamente un producto existente.
     *
     * @param id          ID del producto a actualizar.
     * @param productoDto DTO con los datos nuevos.
     * @return DTO genérico del producto actualizado.
     * @throws ProductException.NotFoundException   si no existe el producto.
     * @throws ProductException.ValidationException si la categoría no existe.
     */
    @Override
    @CacheEvict(key = "#result.id")
    @Transactional
    public GenericProductResponseDto update(String id, PostProductRequestDto productoDto) {
        log.info("SERVICE: Actualizando Producto con id: " + id);

        Product foundProducto = repository.findById(id)
                .orElseThrow(() -> {
                    log.warning("SERVICE: No se encontró producto con id: " + id);
                    return new ProductException.NotFoundException("SERVICE: No se encontró producto con id: " + id);
                });

        var existingCategory = categoryRepository.findByNameIgnoreCase(productoDto.getCategory());
        if (existingCategory.isEmpty()) {
            log.warning("SERVICE: Intento de actualizar un Producto con categoría inexistente");
            throw new ProductException.ValidationException("La categoría " + productoDto.getCategory() + " no existe.");
        }

        // Modificar el producto existente directamente para preservar la version de Hibernate
        foundProducto.setName(productoDto.getName());
        foundProducto.setDescription(productoDto.getDescription());
        foundProducto.setPrice(productoDto.getPrice());
        foundProducto.setStock(productoDto.getStock());
        foundProducto.setCategory(existingCategory.get());
        foundProducto.setCreatorId(productoDto.getCreatorId());
        // No modificamos images aquí, eso lo hace updateOrSaveImage
        // No modificamos id, createdAt ni version  Hibernate los maneja

        Product updatedProductos = repository.save(foundProducto);

        log.info("SERVICE: Producto con id " + updatedProductos.getId() + " actualizado correctamente");
        val commentsDto = mapearComentarios(updatedProductos);
        return mapper.modelToGenericResponseDTO(updatedProductos, commentsDto);
    }

    @Override
    public List<String> getAllCategorias() {
        return categoryRepository.findAll().stream().map(Category::getName).collect(Collectors.toList());
    }

    private List<CommentDto> mapearComentarios(Product producto) {
        return producto.getComments().stream().map((it) -> {
            return mapper.commentToCommentDto(it, userRepository.findById(it.getUserId()).get().getUsername());
        }).toList();
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto.
     * @throws ProductException.NotFoundException si no existe el producto.
     */
    @Override
    @CacheEvict(key = "#id")
    @Transactional
    public void deleteById(String id) {
        log.info("SERVICE: Eliminando Producto con id: " + id);

        Optional<Product> foundProducto = repository.findById(id);
        if (foundProducto.isEmpty()) {
            log.warning("SERVICE: No se encontró Producto con id: " + id);
            throw new ProductException.NotFoundException("SERVICE: No se encontró Producto con id: " + id);
        }
        repository.deleteByIdLogical(id);
    }

    /**
     * Actualiza la imagen de un producto.
     *
     * @param id    ID del producto.
     * @param image Archivo de imagen a actualizar.
     * @return DTO genérico del producto actualizado.
     * @throws ProductException.NotFoundException si no existe el producto.
     */
    @Override
    @Transactional
    public GenericProductResponseDto updateOrSaveImage(String id, List<MultipartFile> image) {
        val foundProducto = repository.findById(id)
                .orElseThrow(() -> new ProductException.NotFoundException("Producto no encontrado con id: " + id));
        log.info("SERVICE: Actualizando imagen de producto por id: " + id);

        // Filtrar archivos vacíos
        List<MultipartFile> validImages = image.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        // Si no hay archivos válidos, no hacer nada y retornar el producto sin cambios
        if (validImages.isEmpty()) {
            log.info("SERVICE: No se subieron imágenes nuevas, manteniendo las existentes");
            val commentsDto = mapearComentarios(foundProducto);
            return mapper.modelToGenericResponseDTO(foundProducto, commentsDto);
        }

        // Si hay imágenes válidas, eliminar las antiguas del almacenamiento
        if (foundProducto.getImages() != null) {
            foundProducto.getImages().forEach(storageService::delete);
        }

        // Guardar las nuevas imágenes en el almacenamiento
        List<String> imageStored = new ArrayList<>(validImages.stream().map(storageService::store).toList());

        // Modificar directamente el producto encontrado para evitar conflictos de versión
        foundProducto.setImages(imageStored);

        var updatedProducto = repository.save(foundProducto);
        val commentsDto = mapearComentarios(updatedProducto);
        return mapper.modelToGenericResponseDTO(updatedProducto, commentsDto);
    }

    public List<Product> findByCreatedAtBetween(LocalDateTime ultimaEjecucion, LocalDateTime ahora) {
        return repository.findAllBycreatedAtBetween(ultimaEjecucion, ahora);
    }

    @Override
    @CacheEvict(key = "#productId")
    public void addComment(String productId, Comment comment) {
        log.info("SERVICE: Agregando comentario al producto con ID: " + productId);

        // Obtener el producto
        Product product = repository.findById(productId)
                .orElseThrow(() -> {
                    log.warning("SERVICE: Producto no encontrado con ID: " + productId);
                    return new ProductException.NotFoundException("Producto no encontrado con ID: " + productId);
                });

        // Agregar el comentario
        product.getComments().add(comment);

        // Guardar el producto actualizado
        repository.save(product);

        log.info("SERVICE: Comentario agregado exitosamente al producto: " + productId);
    }
}
