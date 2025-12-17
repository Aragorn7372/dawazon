package dev.luisvives.dawazon.products.controller;

import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.mapper.ProductMapper;
import dev.luisvives.dawazon.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class ProductsController {
    ProductService productService;
    ProductMapper mapper;

    @Autowired
    public  ProductsController(ProductService productService, ProductMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

    @GetMapping({"/","/products","/products/"})
    public String getProducts(Model model,
                              @RequestParam(required = false) Optional<String> name,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sortBy,
                              @RequestParam(defaultValue = "asc") String direction) {
        log.info("Buscando todos los Productos por nombre: " + name);
        // Creamos el objeto de ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        val products = mapper.pageToDTO(productService.findAll(name,pageable) ,sortBy, direction);
        model.addAttribute("productos", products);
        return "web/productos/lista";
    }

    @GetMapping("/products/{id}")
    public String getProduct(Model model, @PathVariable String id) {
        log.info("Buscando productos por id: " + id);
        val product= productService.getById(id);
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
                              @RequestParam("files") List<MultipartFile> files,
                              BindingResult bindingResult) {
        val productoEdit= productService.update(product.getId(),product);
        val productSaveImg=productService.updateOrSaveImage(product.getId(),files);
        return "redirect:/products/"+productoEdit.getId();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(Model model, @PathVariable String id) {
        productService.deleteById(id);
        return "redirect:/products";
    }

}
