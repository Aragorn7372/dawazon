package dev.luisvives.dawazon.cart.controller;

import dev.luisvives.dawazon.cart.dto.LineRequestDto;
import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import jakarta.validation.Valid;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminPurchasedController {
    CartServiceImpl cartService;

    @Autowired
    public AdminPurchasedController(CartServiceImpl cartService) {
        this.cartService = cartService;
    }

    @GetMapping({ "/ventas", "/ventas/" })
    public String sales(Model model,
            @RequestParam(required = false) Optional<String> name,
            @RequestParam(required = false) Optional<String> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        // TODO: Implementar filtrado por status en el servicio
        val ventas = cartService.findAllSalesAsLines(Optional.empty(), true, pageable);
        val ganancias = cartService.calculateTotalEarnings(Optional.empty(), true);
        model.addAttribute("ventas", ventas);
        model.addAttribute("ganancias", ganancias);
        model.addAttribute("currentStatus", status.orElse(""));
        return "web/cart/ventas";
    }

    @GetMapping("/ventas/{ventaId}/{productId}")
    public String saleDetails(@PathVariable String ventaId,
            @PathVariable String productId,
            @ModelAttribute("currentUserId") Long CurrentId,
            @ModelAttribute("isAdmin") boolean isAdmin,
            Model model) {
        val cartDto = cartService.getSaleLineByIds(ventaId, productId, CurrentId, isAdmin);
        model.addAttribute("venta", cartDto);
        return "web/cart/venta-detalle";
    }

    @GetMapping("/ventas/cancel/{ventaId}/{productId}")
    public String cancel(@PathVariable String ventaId,
            @PathVariable String productId,
            @ModelAttribute("currentUserId") Long CurrentId,
            @ModelAttribute("isAdmin") boolean isAdmin,
            Model model) {
        cartService.cancelSale(ventaId, productId, CurrentId, isAdmin);
        return "redirect:/admin/ventas";
    }

    @GetMapping("/ventas/edit/{ventaId}/{productId}")
    public String edit(@PathVariable String ventaId,
            @PathVariable String productId,
            @ModelAttribute("currentUserId") Long CurrentId,
            @ModelAttribute("isAdmin") boolean isAdmin,
            Model model) {
        val cartDto = cartService.getSaleLineByIds(ventaId, productId, CurrentId, isAdmin);
        model.addAttribute("venta", cartDto);
        return "web/cart/venta-edit";
    }

    @PostMapping("/venta/edit")
    public String edit(@Valid @ModelAttribute("producto") LineRequestDto edit,
            BindingResult bindingResult) {
        cartService.update(edit);
        return "redirect:/admin/ventas";
    }
}
