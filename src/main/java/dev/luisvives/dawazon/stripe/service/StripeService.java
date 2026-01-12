package dev.luisvives.dawazon.stripe.service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para integración con Stripe para procesamiento de pagos.
 * <p>
 * Gestiona la creación de sesiones de checkout con Stripe para procesar
 * los pagos de los carritos de compra.
 * </p>
 */
@Service
public class StripeService {

    /**
     * Clave secreta de la API de Stripe (desde configuración).
     */
    @Value("${stripe.key}")
    private String secretKey;

    /**
     * URL base del servidor (para URLs de éxito y cancelación).
     */
    @Value("${server.url}")
    private String serverUrl;

    /**
     * Inicializa la API de Stripe con la clave secreta.
     * <p>
     * Se ejecuta automáticamente después de la construcción del bean.
     * </p>
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Crea una sesión de checkout en Stripe para un carrito.
     * <p>
     * Genera los line items a partir de las líneas del carrito,
     * configura URLs de éxito/cancelación y crea la sesión de pago.
     * </p>
     *
     * @param cart Carrito de compra para el que crear la sesión.
     * @return URL de la sesión de checkout de Stripe.
     * @throws RuntimeException Si hay error al crear la sesión.
     */
    public String createCheckoutSession(Cart cart) {
        try {
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

            for (CartLine line : cart.getCartLines()) {
                long amount = BigDecimal.valueOf(line.getProductPrice())
                        .multiply(BigDecimal.valueOf(100)).longValue();

                lineItems.add(SessionCreateParams.LineItem.builder()
                        .setQuantity(Long.valueOf(line.getQuantity()))
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(amount)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Producto " + line.getProductId())
                                        .build())
                                .build())
                        .build());
            }

            // URL a la que vuelve si paga bien. Pasamos el ID del carrito para cerrarlo luego.
            String successUrl = serverUrl + "/auth/me/cart/checkout/success/";
            String cancelUrl = serverUrl + "/auth/me/cart/checkout/cancel/";

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(cart.getClient().getEmail())
                    .addAllLineItem(lineItems)
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Error creando sesión de pago en Stripe", e);
        }
    }
}
