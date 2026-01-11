package dev.luisvives.dawazon.common.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test unitario para EmailServiceImpl siguiendo principios FIRST.
 * <p>
 * - Fast: Tests rápidos usando solo mocks
 * - Independent: Cada test es independiente y no depende de otros
 * - Repeatable: Se pueden ejecutar múltiples veces con los mismos resultados
 * - Self-validating: Cada test tiene una validación clara
 * - Timely: Tests escritos junto con el código de producción
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private String testFromEmail;
    private String testToEmail;
    private String testSubject;
    private String testBody;

    @BeforeEach
    void setUp() {
        testFromEmail = "noreply@tienda.dev";
        testToEmail = "user@example.com";
        testSubject = "Test Subject";
        testBody = "Test Body";

        // Establecer el valor de fromEmail usando ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", testFromEmail);
    }

    @Test
    void constructor_whenDependenciesProvided_createsEmailService() {
        // Given
        JavaMailSender mockMailSender = mock(JavaMailSender.class);
        String fromEmail = "test@example.com";

        // When
        EmailServiceImpl service = new EmailServiceImpl(mockMailSender, fromEmail);

        // Then
        assertThat(service).isNotNull();
    }

    @Test
    void sendSimpleEmail_whenValidParameters_sendsEmailSuccessfully() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendSimpleEmail(testToEmail, testSubject, testBody);

        // Then
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getTo()).containsExactly(testToEmail);
        assertThat(capturedMessage.getSubject()).isEqualTo(testSubject);
        assertThat(capturedMessage.getText()).isEqualTo(testBody);
        assertThat(capturedMessage.getFrom()).isEqualTo(testFromEmail);
    }

    @Test
    void sendSimpleEmail_whenMailSenderThrowsException_throwsRuntimeException() {
        // Given
        String errorMessage = "SMTP server error";
        doThrow(new RuntimeException(errorMessage))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThatThrownBy(() -> emailService.sendSimpleEmail(testToEmail, testSubject, testBody))
                .isInstanceOf(RuntimeException.class);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSimpleEmail_whenCalledMultipleTimes_sendsMultipleEmails() {
        // When
        emailService.sendSimpleEmail(testToEmail, testSubject, testBody);
        emailService.sendSimpleEmail("another@example.com", "Another Subject", "Another Body");
        emailService.sendSimpleEmail("third@example.com", "Third Subject", "Third Body");

        // Then
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendHtmlEmail_whenValidParameters_sendsHtmlEmailSuccessfully() {
        // Given
        String htmlBody = "<html><body><h1>Test HTML</h1></body></html>";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // When
        emailService.sendHtmlEmail(testToEmail, testSubject, htmlBody);

        // Then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    void sendHtmlEmail_whenMessagingExceptionOccurs_throwsRuntimeException() {
        // Given
        String htmlBody = "<html><body><h1>Test HTML</h1></body></html>";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doThrow(new RuntimeException("Messaging error"))
                .when(mailSender).send(any(MimeMessage.class));

        // When & Then
        assertThatThrownBy(() -> emailService.sendHtmlEmail(testToEmail, testSubject, htmlBody))
                .isInstanceOf(RuntimeException.class);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    void sendHtmlEmail_whenCalledMultipleTimes_sendsMultipleHtmlEmails() {
        // Given
        String htmlBody = "<html><body><h1>Test HTML</h1></body></html>";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // When
        emailService.sendHtmlEmail(testToEmail, testSubject, htmlBody);
        emailService.sendHtmlEmail("another@example.com", "Another Subject", htmlBody);

        // Then
        verify(mailSender, times(2)).createMimeMessage();
        verify(mailSender, times(2)).send(mockMimeMessage);
    }

    @Test
    void sendSimpleEmail_verifyNoOtherInteractions() {
        // When
        emailService.sendSimpleEmail(testToEmail, testSubject, testBody);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
        verifyNoMoreInteractions(mailSender);
    }

    @Test
    void sendHtmlEmail_verifyNoOtherInteractions() {
        // Given
        String htmlBody = "<html><body><h1>Test HTML</h1></body></html>";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // When
        emailService.sendHtmlEmail(testToEmail, testSubject, htmlBody);

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mockMimeMessage);
        verifyNoMoreInteractions(mailSender);
    }
}
