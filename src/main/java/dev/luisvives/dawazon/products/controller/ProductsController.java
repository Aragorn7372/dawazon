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

@Controller
@Slf4j
public class ProductsController {
    ProductService productService;
    ProductMapper mapper;

    @Autowired
    public ProductsController(ProductService productService, ProductMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

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

    @GetMapping("/products/{id}")
    public String getProduct(Model model, @PathVariable String id) {
        log.info("Buscando productos por id: " + id);
        val product = productService.getById(id);
        model.addAttribute("producto", product);
        return "web/productos/producto";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/products/edit/{id}")
    public String editProduct(Model model, @PathVariable String id) {
        log.info("Editando el producto con id: " + id);

        val product = productService.getById(id);

        model.addAttribute("product", product);

        return "web/productos/productSaveEdit";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/edit/")
    public String editProduct(@Valid @ModelAttribute("producto") PostProductRequestDto product,
            @RequestParam("file") List<MultipartFile> files,
            BindingResult bindingResult) {
        val productoEdit = productService.update(product.getId(), product);
        val productSaveImg = productService.updateOrSaveImage(product.getId(), files);
        return "redirect:/products/" + productoEdit.getId();
    }

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(Model model, @PathVariable String id) {
        productService.deleteById(id);
        return "redirect:/products";
    }

}
