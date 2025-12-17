package dev.luisvives.dawazon.common.email;

/**
 * Interfaz que representa las funciones que puede realizar el servicio de emails.
 */
public interface EmailService {

    /**
     * Envía un email simple (texto plano).
     */
    void sendSimpleEmail(String to, String subject, String body);

    /**
     * Envía un email con HTML.
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);
}
