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

@Service
public class StripeService {

    @Value("${stripe.key}")
    private String secretKey;

    @Value("${server.url}")
    private String serverUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

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
            String successUrl = serverUrl + "/auth/me/cart/checkout/success/" + cart.getId();
            String cancelUrl = serverUrl + "/auth/me/cart/checkout/cancel/" + cart.getId();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(cart.getClient().getEmail()) // Opcional
                    .addAllLineItem(lineItems)
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Error creando sesión de pago en Stripe", e);
        }
    }
}
