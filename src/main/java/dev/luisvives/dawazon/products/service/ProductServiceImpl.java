package dev.luisvives.dawazon.products.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.dawazon.common.service.Service;
import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.products.dto.CommentDto;
import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.mapper.ProductMapper;
import dev.luisvives.dawazon.products.models.Category;
import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.products.repository.CategoryRepository;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.users.repository.UserRepository;
import jakarta.persistence.criteria.Join;
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
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementación del servicio de productos (Producto).
 * <p>
 * Esta capa actúa como puente entre el controlador y los repositorios de productos y categorías.
 * Contiene la lógica de negocio, manejo de cache, validaciones, integridad referencial,
 * y envía notificaciones vía WebSocket cuando hay cambios en los productos.
 * </p>
 *
 * <p>
 * Anotaciones:
 * <ul>
 *     <li>{@link Service}: Indica que esta clase es un servicio gestionado por Spring.</li>
 *     <li>{@link CacheConfig}: Configura la caché con nombre "productos".</li>
 * </ul>
 * </p>
 *
 * @see ProductService
 * @see ProductMapper
 * @see StorageService
 */
@org.springframework.stereotype.Service
@CacheConfig(cacheNames = {"productos"})
public class ProductServiceImpl implements ProductService {

    private final Logger log = Logger.getLogger(ProductServiceImpl.class.getName());

    /**
     * Repositorio de productos para operaciones CRUD
     */
    private final ProductRepository repository;

    /**
     * Repositorio de categorías para validaciones de integridad referencial
     */
    private final CategoryRepository categoryRepository;
    /**
     * Repositorio de categorías para validaciones de integridad referencial
     */
    private final UserRepository userRepository;
    /**
     * Servicio de almacenamiento para manejar imágenes
     */
    private final StorageService storageService;


    /**
     * Mapper de Jackson para serializar objetos a JSON
     */
    ObjectMapper jacksonMapper;
    ProductMapper mapper;

    /**
     * Constructor que inyecta dependencias necesarias.
     *
     * @param repository         Repositorio de productos
     * @param categoryRepository Repositorio de categorías
     * @param storageService     Servicio de almacenamiento de imágenes
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
     * Busca productos aplicando filtros opcionales por nombre, precio máximo y categoría.
     *
     * @param name     Filtro opcional por nombre
     * @param pageable Paginación y ordenación
     * @return Página de productos que cumplen los filtros
     */
    @Override
    public Page<Product> findAll(Optional<String> name,
                                 Pageable pageable) {
        var isDeleted=true;
        Specification<Product> specNameProducto = (root, query, criteriaBuilder) ->
                name.map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                                "%" + n.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Product> specIsDeleted = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), isDeleted);

        Specification<Product> criterio = Specification.allOf(
                specNameProducto,
                specIsDeleted
        );

        return repository.findAll(criterio, pageable);
    }

    /**
     * Recupera un producto por su ID.
     * <p>
     * Se utiliza caché para mejorar el rendimiento.
     * </p>
     *
     * @param id Identificador del producto
     * @return DTO genérico del producto
     * @throws ProductException.NotFoundException si no existe el producto
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
        val commentsDto= mapearComentarios(productoFound);

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
     * Valida integridad referencial con la categoría y envía notificación de creación.
     * </p>
     *
     * @param productoDto DTO con datos del producto
     * @return DTO genérico del producto creado
     * @throws ProductException.ValidationException si la categoría no existe
     */
    @Override
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
        val commentsDto=mapearComentarios(savedProducto);
        return mapper.modelToGenericResponseDTO(savedProducto, commentsDto);
    }

    /**
     * Actualiza completamente un producto existente.
     *
     * @param id          ID del producto a actualizar
     * @param productoDto DTO con los datos nuevos
     * @return DTO genérico del producto actualizado
     * @throws ProductException.NotFoundException   si no existe el producto
     * @throws ProductException.ValidationException si la categoría no existe
     */
    @Override
    @CacheEvict(key = "#result.id")
    public GenericProductResponseDto update(String id, PostProductRequestDto productoDto) {
        log.info("SERVICE: Actualizando Producto con id: " + id);

        Optional<Product> foundProducto = repository.findById(id);
        if (foundProducto.isEmpty()) {
            log.warning("SERVICE: No se encontró producto con id: " + id);
            throw new ProductException.NotFoundException("SERVICE: No se encontró producto con id: " + id);
        }

        var existingCategory = categoryRepository.findByNameIgnoreCase(productoDto.getCategory());
        if (existingCategory.isEmpty()) {
            log.warning("SERVICE: Intento de actualizar un Producto con categoría inexistente");
            throw new ProductException.ValidationException("La categoría " + productoDto.getCategory() + " no existe.");
        }

        Product productoModel = mapper.postPutDTOToModel(productoDto);
        productoModel.setId(id);
        productoModel.setCreatedAt(foundProducto.get().getCreatedAt());
        productoModel.setCategory(existingCategory.get());

        Product updatedProductos = repository.save(productoModel);


        log.info("SERVICE: Producto con id " + updatedProductos.getId() + " actualizado correctamente");
        val commentsDto= mapearComentarios(updatedProductos);
        return mapper.modelToGenericResponseDTO(updatedProductos,commentsDto);
    }

    private List<CommentDto> mapearComentarios(Product producto) {
        return producto.getComments().stream().map((it)->{
            return  mapper.commentToCommentDto(it,userRepository.findById(it.getUserId()).get().getUsername());
        }).toList();
    }


    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto
     * @throws ProductException.NotFoundException si no existe el producto
     */
    @Override
    @CacheEvict(key = "#id")
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
     * @param id    ID del producto
     * @param image Archivo de imagen a actualizar
     * @return DTO genérico del producto actualizado
     * @throws ProductException.NotFoundException si no existe el producto
     */
    @Override
    public GenericProductResponseDto updateOrSaveImage(String id, List<MultipartFile> image) {
        val foundProducto = repository.findById(id)
                .orElseThrow(() -> new ProductException.NotFoundException("Producto no encontrado con id: " + id));
        log.info("Actualizando imagen de producto por id: " + id);

        if (foundProducto.getImages() != null) {
            foundProducto.getImages().forEach(storageService::delete);
        }

        List<String> imageStored = image.stream().map(storageService::store).toList();

        Product productoToUpdate = Product.builder()
                .id(foundProducto.getId())
                .name(foundProducto.getName())
                .price(foundProducto.getPrice())
                .category(foundProducto.getCategory())
                .description(foundProducto.getDescription())
                .images(imageStored)
                .createdAt(foundProducto.getCreatedAt())
                .updatedAt(foundProducto.getUpdatedAt())
                .build();

        var updatedProducto = repository.save(productoToUpdate);
        val commentsDto= mapearComentarios(updatedProducto);
        return mapper.modelToGenericResponseDTO(updatedProducto,commentsDto);
    }



    public List<Product> findByCreatedAtBetween(LocalDateTime ultimaEjecucion, LocalDateTime ahora) {
        return repository.findAllByFechaCreacionBetween(ultimaEjecucion, ahora);
    }
}
