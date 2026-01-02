package dev.luisvives.dawazon.common.cleanup;

import dev.luisvives.dawazon.cart.service.CartService;
import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para limpiar carritos abandonados durante el checkout.
 * <p>
 * Se ejecuta cada 2 minutos para verificar y limpiar carritos con
 * sesiones de pago expiradas (más de 5 minutos sin completar).
 * </p>
 */
@Component
@Slf4j
public class CartCleanupScheduler {

    /**
     * Servicio de carritos para operaciones de limpieza.
     */
    private final CartServiceImpl cartService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param cartService Servicio de carritos
     */
    @Autowired
    public CartCleanupScheduler(CartServiceImpl cartService) {
        this.cartService = cartService;
    }

    /**
     * Tarea programada que limpia carritos con checkout expirado.
     * <p>
     * Se ejecuta cada 2 minutos para restaurar stock de carritos abandonados.
     * </p>
     */
    @Scheduled(fixedRate = 120000) // 120000ms = 2 minutos
    public void cleanupExpiredCheckoutsAlternative() {
        log.debug("🔍 Ejecutando limpieza de carritos expirados...");
        cartService.cleanupExpiredCheckouts();
    }
}