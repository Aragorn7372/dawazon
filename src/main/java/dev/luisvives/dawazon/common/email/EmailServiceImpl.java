package dev.luisvives.dawazon.common.email;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de envío de correos electrónicos.
 * <p>
 * Esta clase implementa la interfaz {@link EmailService} y utiliza {@link JavaMailSender}
 * para enviar correos en formato texto plano o en formato HTML.
 * </p>
 *
 * <p>
 * Está anotada con {@link Service}, por lo que Spring la detecta automáticamente como
 * componente del contenedor de servicios y la inyecta donde sea necesaria.
 * </p>
 *
 * <p>
 * Admite el envío de correos con remitente configurable mediante la propiedad
 * <code>app.mail.from</code> del archivo de configuración.
 * </p>
 */
@Service
public class EmailServiceImpl implements EmailService {

    /**
     * Logger para registrar información y errores durante el envío de correos.
     */
    private final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    /**
     * Objeto proporcionado por Spring para enviar correos electrónicos.
     * Se inyecta automáticamente mediante la configuración de Spring Boot Mail.
     */
    private final JavaMailSender mailSender;

    /**
     * Dirección de correo electrónico utilizada como remitente (por defecto: {@code noreply@tienda.dev}).
     */
    private final String fromEmail;

    /**
     * Constructor de la clase.
     *
     * @param mailSender instancia de {@link JavaMailSender} inyectada por Spring para el envío de correos.
     * @param fromEmail  dirección de correo electrónico del remitente, configurable mediante la propiedad
     *                   <code>app.mail.from</code> en <code>application.properties</code> o <code>application.yml</code>.
     */
    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${app.mail.from:noreply@tienda.dev}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Envía un correo electrónico en formato texto plano.
     * <p>
     * Este método construye un objeto {@link SimpleMailMessage} y lo envía utilizando
     * el {@link JavaMailSender} configurado por Spring Boot.
     * </p>
     *
     * @param to      dirección de correo electrónico del destinatario.
     * @param subject asunto del correo.
     * @param body    cuerpo del mensaje en texto plano.
     * @throws RuntimeException si ocurre un error al enviar el correo.
     */
    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            logger.info("Enviando email simple a: {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);

            mailSender.send(message);
            logger.info("Email simple enviado correctamente a: {}", to);

        } catch (Exception e) {
            logger.error("Error enviando email simple a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    /**
     * Envía un correo electrónico en formato HTML.
     * <p>
     * Este método utiliza {@link MimeMessageHelper} para crear un mensaje MIME
     * que admite contenido HTML, codificación UTF-8 y adjuntos (aunque no se usan aquí).
     * </p>
     *
     * @param to       dirección de correo electrónico del destinatario
     * @param subject  asunto del correo
     * @param htmlBody cuerpo del mensaje en formato HTML
     * @throws RuntimeException si ocurre un error al crear o enviar el mensaje
     */
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            logger.info("Enviando email HTML a: {}", to);

            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = el contenido es HTML
            helper.setFrom(fromEmail);

            mailSender.send(message);
            logger.info("Email HTML enviado correctamente a: {}", to);

        } catch (MessagingException e) {
            logger.error("Error enviando email HTML a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error enviando email HTML: " + e.getMessage(), e);
        }
    }
}
