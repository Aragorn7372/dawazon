package dev.luisvives.dawazon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación Dawazon.
 * <p>
 * Aplicación Spring Boot de e-commerce que incluye gestión de productos,
 * carritos de compra, usuarios y procesamiento de pagos con Stripe.
 * Habilita caché y tareas programadas.
 * </p>
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class DawazonApplication {

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        SpringApplication.run(DawazonApplication.class, args);
    }

}
