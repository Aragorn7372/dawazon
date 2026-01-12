package dev.luisvives.dawazon.common.email;

/**
 * Interfaz que define el servicio de envío de emails.
 * <p>
 * Soporta envío de emails en formato texto plano y HTML.
 * </p>
 */
public interface EmailService {

    /**
     * Envía un email simple en formato texto plano.
     *
     * @param to      Destinatario.
     * @param subject Asunto del email.
     * @param body    Cuerpo del mensaje en texto plano.
     */
    void sendSimpleEmail(String to, String subject, String body);

    /**
     * Envía un email con contenido HTML.
     *
     * @param to       Destinatario.
     * @param subject  Asunto del email.
     * @param htmlBody Cuerpo del mensaje en formato HTML.
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);
}
