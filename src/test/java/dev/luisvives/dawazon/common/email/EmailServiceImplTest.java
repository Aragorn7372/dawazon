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

        ReflectionTestUtils.setField(emailService, "fromEmail", testFromEmail);
    }

    @Test
    void constructorwhenDependenciesProvidedcreatesEmailService() {
        JavaMailSender mockMailSender = mock(JavaMailSender.class);
        String fromEmail = "test@example.com";

        EmailServiceImpl service = new EmailServiceImpl(mockMailSender, fromEmail);

        assertThat(service).isNotNull();
    }

    @Test
    void sendSimpleEmailwhenValidParameterssendsEmailSuccessfully() {
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSimpleEmail(testToEmail, testSubject, testBody);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getTo()).containsExactly(testToEmail);
        assertThat(capturedMessage.getSubject()).isEqualTo(testSubject);
        assertThat(capturedMessage.getText()).isEqualTo(testBody);
        assertThat(capturedMessage.getFrom()).isEqualTo(testFromEmail);
    }

    @Test
    void sendSimpleEmailwhenMailSenderThrowsExceptionthrowsRuntimeException() {
        String errorMessage = "SMTP server error";
        doThrow(new RuntimeException(errorMessage))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendSimpleEmail(testToEmail, testSubject, testBody))
                .isInstanceOf(RuntimeException.class);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSimpleEmailwhenCalledMultipleTimessendsMultipleEmails() {
        emailService.sendSimpleEmail(testToEmail, testSubject, testBody);
        emailService.sendSimpleEmail("another@example.com", "Another Subject", "Another Body");
        emailService.sendSimpleEmail("third@example.com", "Third Subject", "Third Body");

        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendHtmlEmailwhenValidParameterssendsHtmlEmailSuccessfully() {
        String htmlBody = "<html><body><h1>Test HTML</h1></body></html>";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        emailService.sendHtmlEmail(testToEmail, testSubject, htmlBody);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    void sendHtmlEmailWhenMessagingExceptionOccursThrowsRuntimeException() {
        String htmlBody = "<html><body><h1>Test HTML</h1></body></html>";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doThrow(new RuntimeException("Messaging error"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> emailService.sendHtmlEmail(testToEmail, testSubject, htmlBody))
                .isInstanceOf(RuntimeException.class);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    void sendHtmlEmailWhenCalledMultipleTimesSendsMultipleHtmlEmails() {
        String htmlBody = "<html><body><h1>Test HTML</h1></body></html>";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        emailService.sendHtmlEmail(testToEmail, testSubject, htmlBody);
        emailService.sendHtmlEmail("another@example.com", "Another Subject", htmlBody);

        verify(mailSender, times(2)).createMimeMessage();
        verify(mailSender, times(2)).send(mockMimeMessage);
    }

    @Test
    void sendSimpleEmailVerifyNoOtherInteractions() {
        emailService.sendSimpleEmail(testToEmail, testSubject, testBody);

        verify(mailSender).send(any(SimpleMailMessage.class));
        verifyNoMoreInteractions(mailSender);
    }

    @Test
    void sendHtmlEmailVerifyNoOtherInteractions() {
        String htmlBody = "<html><body><h1>Test HTML</h1></body></html>";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        emailService.sendHtmlEmail(testToEmail, testSubject, htmlBody);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mockMimeMessage);
        verifyNoMoreInteractions(mailSender);
    }
}
