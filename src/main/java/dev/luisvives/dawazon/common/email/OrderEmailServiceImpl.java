package dev.luisvives.dawazon.common.email;

import dev.luisvives.dawazon.cart.models.Cart;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Servicio encargado de gestionar el envío de correos electrónicos relacionados con los pedidos.
 * <p>
 * Implementa la interfaz {@link OrderEmailService} para enviar correos electrónicos de confirmación
 * de pedidos tanto en formato HTML simple como en formato HTML completo.
 * </p>
 *
 * <p>Utiliza {@link EmailService} para el envío real de los correos.</p>
 */
@Service
public class OrderEmailServiceImpl implements OrderEmailService {

    /**
     * Logger para registrar información y errores relacionados con el envío de correos.
     */
    private final Logger logger = LoggerFactory.getLogger(OrderEmailServiceImpl.class);

    /**
     * Servicio utilizado para el envío de correos electrónicos.
     */
    private final EmailService emailService;

    /**
     * Constructor de la clase.
     *
     * @param emailService servicio encargado de enviar correos electrónicos
     */
    public OrderEmailServiceImpl(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Envía un correo de confirmación de pedido en formato HTML simple.
     * <p>
     * El correo contiene información básica del pedido, datos del cliente y los productos solicitados.
     * </p>
     *
     * @param pedido objeto {@link Cart} que contiene los datos del pedido confirmado
     */
    @Override
    public void enviarConfirmacionPedido(Cart pedido) {
        try {
            logger.info("Enviando confirmación HTML simple de pedido {} al cliente {}",
                    pedido.getId(), pedido.getClient().getEmail());

            String subject = "Confirmación de tu pedido #" + pedido.getId();
            String htmlBody = crearCuerpoEmailPedidoHtmlSimple(pedido);

            emailService.sendHtmlEmail(
                    pedido.getClient().getEmail(),
                    subject,
                    htmlBody);

            logger.info("Email HTML simple de confirmación enviado correctamente para el pedido {}", pedido.getId());

        } catch (Exception e) {
            logger.error("Error enviando email de confirmación para el pedido {}: {}",
                    pedido.getId(), e.getMessage());
        }
    }

    /**
     * Envía un correo de confirmación de pedido en formato HTML completo y estilizado.
     * <p>
     * Este método genera un correo con diseño y estilos CSS más avanzados.
     * Actualmente reutiliza el método {@link #crearCuerpoEmailPedidoHtmlSimple(Cart)} como base.
     * </p>
     *
     * @param pedido objeto {@link Cart} que contiene los datos del pedido confirmado
     */
    @Override
    public void enviarConfirmacionPedidoHtml(Cart pedido) {
        try {
            logger.info("Enviando confirmación HTML completa de pedido {} al cliente {}",
                    pedido.getId(), pedido.getClient().getEmail());

            String subject = "Confirmación de tu pedido #" + pedido.getId();
            String htmlBody = crearCuerpoEmailPedidoHtmlCompleto(pedido);

            emailService.sendHtmlEmail(
                    pedido.getClient().getEmail(),
                    subject,
                    htmlBody);

            logger.info("Email HTML completo de confirmación enviado correctamente para el pedido {}", pedido.getId());

        } catch (Exception e) {
            logger.error("Error enviando email HTML de confirmación para el pedido {}: {}",
                    pedido.getId(), e.getMessage());
        }
    }

    /**
     * Crea el cuerpo del correo electrónico en formato HTML simple.
     * <p>
     * Incluye:
     * <ul>
     * <li>Información del pedido (número, fecha, estado)</li>
     * <li>Datos del cliente (nombre, email, teléfono)</li>
     * <li>Dirección de entrega</li>
     * <li>Lista de productos con cantidad, precio y total</li>
     * <li>Resumen final del pedido</li>
     * </ul>
     * </p>
     *
     * @param pedido objeto {@link Cart} con la información del pedido
     * @return cuerpo del correo en formato HTML
     */
    private String crearCuerpoEmailPedidoHtmlSimple(Cart pedido) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

        String lineasPedidoHtml = pedido.getCartLines().stream()
                .map(linea -> String.format("""
                        <li>
                            <strong>Producto ID:</strong> %s |
                            <strong>Cantidad:</strong> %d |
                            <strong>Precio:</strong> %s |
                            <strong>Total:</strong> %s
                        </li>
                        """,
                        linea.getProductId(),
                        linea.getQuantity(),
                        currencyFormatter.format(linea.getProductPrice()),
                        currencyFormatter.format(linea.getTotalPrice())))
                .collect(Collectors.joining(""));

        return String.format(
                """
                        <!DOCTYPE html>
                        <html lang="es">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Confirmación de Pedido</title>
                        </head>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px;">
                            <div style="max-width: 600px; margin: 0 auto;">

                                <h1 style="color: #4CAF50; text-align: center;">¡Pedido Confirmado! 🎉</h1>

                                <p><strong>¡Hola %s!</strong></p>
                                <p>Tu pedido ha sido confirmado y está siendo procesado.</p>

                                <hr style="border: 1px solid #ddd; margin: 20px 0;">

                                <h2 style="color: #4CAF50;">Información del Pedido</h2>
                                <ul>
                                    <li><strong>Número:</strong> #%s</li>
                                    <li><strong>Fecha:</strong> %s</li>
                                    <li><strong>Estado:</strong> <span style="color: #4CAF50;">Confirmado</span></li>
                                </ul>

                                <h2 style="color: #4CAF50;">👤 Datos del Cliente</h2>
                                <ul>
                                    <li><strong>Nombre:</strong> %s</li>
                                    <li><strong>Email:</strong> %s</li>
                                    <li><strong>Teléfono:</strong> %s</li>
                                </ul>

                                <h2 style="color: #4CAF50;">🚚 Dirección de Entrega</h2>
                                <p>
                                    %s, %s<br>
                                    %s %s<br>
                                    %s, %s
                                </p>

                                <h2 style="color: #4CAF50;">🛒 Detalles del Pedido</h2>
                                <ul>
                                    %s
                                </ul>

                                <div style="background-color: #4CAF50; color: white; padding: 15px; text-align: center; border-radius: 5px; margin: 20px 0;">
                                    <h3 style="margin: 0;">Total de artículos: %d | TOTAL: %s</h3>
                                </div>

                                <p><strong>🕐 Tu pedido será procesado en las próximas 24-48 horas.</strong></p>
                                <p>📧 Te mantendremos informado sobre el estado de tu envío.</p>

                                <hr style="border: 1px solid #ddd; margin: 20px 0;">

                                <p style="text-align: center;">
                                    <strong>¡Gracias por confiar en nosotros!</strong><br>
                                    <em>El equipo de Tienda</em>
                                </p>

                                <p style="text-align: center; font-size: 12px; color: #666;">
                                    Este es un email automático, por favor no respondas a este mensaje.
                                </p>

                            </div>
                        </body>
                        </html>
                        """,
                pedido.getClient().getName(),
                pedido.getId(),
                pedido.getCreatedAt().format(formatter),
                pedido.getClient().getName(),
                pedido.getClient().getEmail(),
                pedido.getClient().getPhone(),
                pedido.getClient().getAddress().getStreet(),
                pedido.getClient().getAddress().getNumber(),
                pedido.getClient().getAddress().getPostalCode(),
                pedido.getClient().getAddress().getCity(),
                pedido.getClient().getAddress().getProvince(),
                pedido.getClient().getAddress().getCountry(),
                lineasPedidoHtml,
                pedido.getTotalItems(),
                currencyFormatter.format(pedido.getTotal()));
    }

    /**
     * Crea el cuerpo del correo electrónico en formato HTML completo y con estilos CSS avanzados.
     * <p>
     * Actualmente este método reutiliza el cuerpo generado por {@link #crearCuerpoEmailPedidoHtmlSimple(Cart)}.
     * Se deja preparado para futuras mejoras de diseño o personalización.
     * </p>
     *
     * @param pedido objeto {@link Cart} con los datos del pedido
     * @return cuerpo del correo en formato HTML completo
     */
    private String crearCuerpoEmailPedidoHtmlCompleto(Cart pedido) {
        return crearCuerpoEmailPedidoHtmlSimple(pedido);
    }
}