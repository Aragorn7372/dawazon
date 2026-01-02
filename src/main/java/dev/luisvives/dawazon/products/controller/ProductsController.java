package dev.luisvives.dawazon.products.controller;

import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.models.Comment;
import dev.luisvives.dawazon.products.mapper.ProductMapper;
import dev.luisvives.dawazon.products.service.ProductService;
import dev.luisvives.dawazon.users.models.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador web para gestionar productos en la aplicación Dawazon.
 * <p>
 * Este controlador maneja las peticiones HTTP relacionadas con productos,
 * incluyendo listado, detalle, edición, eliminación y comentarios.
 * Utiliza Thymeleaf para renderizar vistas HTML.
 * </p>
 *
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Listado de productos con filtros y paginación</li>
 * <li>Visualización de detalles de producto</li>
 * <li>Edición de productos (solo ADMIN)</li>
 * <li>Eliminación lógica de productos (solo ADMIN)</li>
 * <li>Sistema de comentarios (usuarios autenticados)</li>
 * </ul>
 * </p>
 *
 * @see ProductService
 * @see ProductMapper
 */
@Controller
@Slf4j
public class ProductsController {
    /**
     * Servicio de productos para lógica de negocio.
     */
    ProductService productService;

    /**
     * Mapper para transformar entidades a DTOs.
     */
    ProductMapper mapper;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param productService Servicio de productos
     * @param mapper         Mapper de productos
     */
    @Autowired
    public ProductsController(ProductService productService, ProductMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

    /**
     * Obtiene y muestra el listado de productos con filtros y paginación.
     * <p>
     * Endpoint público accesible desde la raíz, /products y /products/.
     * Permite filtrar por nombre y categoría, con soporte de paginación y
     * ordenamiento.
     * </p>
     *
     * @param model     Modelo de Spring MVC para pasar datos a la vista
     * @param name      Filtro opcional por nombre de producto
     * @param category  Filtro opcional por categoría
     * @param page      Número de página (por defecto 0)
     * @param size      Tamaño de página (por defecto 10)
     * @param sortBy    Campo de ordenamiento (por defecto "id")
     * @param direction Dirección de ordenamiento: asc o desc (por defecto "asc")
     * @return Nombre de la vista Thymeleaf "web/productos/lista"
     */
    @GetMapping({ "/", "/products", "/products/" })
    public String getProducts(Model model,
            @RequestParam(required = false) Optional<String> name,
            @RequestParam(value = "categoria", required = false) Optional<String> category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Buscando todos los Productos por nombre: " + name);
        // Creamos el objeto de ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        val products = mapper.pageToDTO(productService.findAll(name, category, pageable), sortBy, direction);
        model.addAttribute("productos", products);
        return "web/productos/lista";
    }

    /**
     * Muestra el detalle de un producto específico.
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del producto a mostrar
     * @return Nombre de la vista Thymeleaf "web/productos/producto"
     */
    @GetMapping("/products/{id}")
    public String getProduct(Model model, @PathVariable String id) {
        log.info("Buscando productos por id: " + id);
        val product = productService.getById(id);
        model.addAttribute("producto", product);
        return "web/productos/producto";
    }

    /**
     * Muestra el formulario de edición de un producto.
     * <p>
     * Solo accesible para usuarios con rol ADMIN.
     * </p>
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del producto a editar
     * @return Nombre de la vista Thymeleaf "web/productos/productSaveEdit"
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/products/edit/{id}")
    public String editProduct(Model model, @PathVariable String id) {
        log.info("Editando el producto con id: " + id);

        val product = productService.getById(id);

        model.addAttribute("product", product);

        return "web/productos/productSaveEdit";
    }

    /**
     * Procesa la edición de un producto existente.
     * <p>
     * Actualiza los datos del producto y sus imágenes. Solo para ADMIN.
     * </p>
     *
     * @param product       DTO con los nuevos datos del producto
     * @param files         Archivos de imágenes a actualizar
     * @param bindingResult Resultado de validación
     * @return Redirección a la página de detalle del producto
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/edit/")
    public String editProduct(@Valid @ModelAttribute("producto") PostProductRequestDto product,
            @RequestParam("file") List<MultipartFile> files,
            BindingResult bindingResult) {
        val productoEdit = productService.update(product.getId(), product);
        val productSaveImg = productService.updateOrSaveImage(product.getId(), files);
        return "redirect:/products/" + productoEdit.getId();
    }

    /**
     * Publica un comentario en un producto.
     * <p>
     * Endpoint REST que retorna JSON. Solo usuarios con rol USER pueden comentar.
     * El comentario se crea como no verificado por defecto.
     * </p>
     *
     * @param id         ID del producto a comentar
     * @param comment    Contenido del comentario
     * @param recomended Si el usuario recomienda el producto
     * @param model      Modelo con información del usuario actual
     * @return ResponseEntity con el resultado de la operación (success/error)
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/products/{id}/comentarios")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> postComment(
            @PathVariable String id,
            @RequestParam String comment,
            @RequestParam boolean recomended,
            Model model) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Obtener el usuario autenticado
            val user = (User) model.getAttribute("currentUser");

            // Crear el comentario
            Comment newComment = Comment.builder()
                    .userId(user.getId())
                    .content(comment)
                    .recommended(recomended)
                    .verified(false)
                    .build();

            // Agregar el comentario al producto
            productService.addComment(id, newComment);

            response.put("success", true);
            response.put("message", "Comentario publicado correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al publicar comentario: " + e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al publicar el comentario: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Elimina lógicamente un producto.
     * <p>
     * Solo usuarios ADMIN pueden eliminar productos.
     * El producto se marca como eliminado pero no se borra de la base de datos.
     * </p>
     *
     * @param model Modelo de Spring MVC
     * @param id    ID del producto a eliminar
     * @return Redirección al listado de productos
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(Model model, @PathVariable String id) {
        productService.deleteById(id);
        return "redirect:/products";
    }

}
