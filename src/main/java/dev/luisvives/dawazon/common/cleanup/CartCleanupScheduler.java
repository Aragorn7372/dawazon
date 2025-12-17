package dev.luisvives.dawazon.common.cleanup;

import dev.luisvives.dawazon.cart.service.CartService;
import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import lombok.extern.slf4j. Slf4j;
import org. springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para limpiar carritos abandonados durante el checkout
 * Se ejecuta cada minuto para verificar si hay sesiones expiradas
 */
@Component
@Slf4j
public class CartCleanupScheduler {

    private final CartServiceImpl cartService;

    @Autowired
    public CartCleanupScheduler(CartServiceImpl cartService) {
        this.cartService = cartService;
    }



    @Scheduled(fixedRate = 120000) // 120000ms = 2 minutos
    public void cleanupExpiredCheckoutsAlternative() {
         log.debug("🔍 Ejecutando limpieza de carritos expirados...");
         cartService.cleanupExpiredCheckouts();
    }
}