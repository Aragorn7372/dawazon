package dev.luisvives.dawazon.cart.controller;

import dev.luisvives.dawazon.cart.dto.LineRequestDto;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import dev.luisvives.dawazon.users.models.User;
import jakarta.validation.Valid;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@PreAuthorize("ADMIN")
public class AdminPurchasedController {
    CartServiceImpl cartService;
    @Autowired
    public AdminPurchasedController(CartServiceImpl cartService) {
        this.cartService = cartService;
    }

    @GetMapping({"/ventas", "/ventas/"})
    public String sales(Model model,
                        @RequestParam(required = false) Optional<String> name,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        val venta = cartService.findAllSalesAsLines(Optional.empty(),true,pageable);
        val ganancias = cartService.findAllSalesAsLines(Optional.empty(),true,pageable);
        model.addAttribute("venta", venta);
        model.addAttribute("ganancias", ganancias);
        return "/web/cart/ventas";
    }

    @GetMapping("/ventas/{ventaId}/{productId}")
    public String saleDetails(@PathVariable String ventaId,
                              @PathVariable String productId,
                              @ModelAttribute("currentUserId") Long CurrentId,
                              @ModelAttribute("isAdmin") boolean isAdmin,
                              Model model) {
        val cartDto=cartService.getSaleLineByIds(ventaId,productId,CurrentId,isAdmin);
        model.addAttribute("venta",cartDto);
        return "/web/cart/venta-detalle";
    }

    @GetMapping("/ventas/edit/{ventaId}/{productoId}")
    public String edit(@PathVariable String ventaId,
                       @PathVariable String productId,
                       @ModelAttribute("currentUserId") Long CurrentId,
                       @ModelAttribute("isAdmin") boolean isAdmin,
                       Model model){
        val cartDto=cartService.getSaleLineByIds(ventaId,productId,CurrentId,isAdmin);
        model.addAttribute("venta",cartDto);
        return "/web/cart/venta-edit";
    }

    @PostMapping("/venta/edit")
    public String edit(@Valid @ModelAttribute("producto")LineRequestDto edit,
                       BindingResult bindingResult) {
        cartService.update(edit);
        return "redirect:/ventas";
    }
}
