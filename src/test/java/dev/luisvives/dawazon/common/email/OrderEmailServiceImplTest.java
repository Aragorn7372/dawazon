package dev.luisvives.dawazon.common.email;

import dev.luisvives.dawazon.cart.models.Address;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.cart.models.Client;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitario para OrderEmailServiceImpl siguiendo principios FIRST.
 * <p>
 * - Fast: Tests rápidos usando solo mocks
 * - Independent: Cada test es independiente y no depende de otros
 * - Repeatable: Se pueden ejecutar múltiples veces con los mismos resultados
 * - Self-validating: Cada test tiene una validación clara
 * - Timely: Tests escritos junto con el código de producción
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class OrderEmailServiceImplTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderEmailServiceImpl orderEmailService;

    private Cart testCart;
    private Client testClient;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        // Crear dirección de prueba
        testAddress = Address.builder()
                .street("Calle Principal")
                .number((short) 123)
                .postalCode(28001)
                .city("Madrid")
                .province("Madrid")
                .country("España")
                .build();

        // Crear cliente de prueba
        testClient = Client.builder()
                .name("Juan Pérez")
                .email("juan.perez@example.com")
                .phone("+34 600 123 456")
                .address(testAddress)
                .build();

        // Crear líneas de carrito
        CartLine line1 = CartLine.builder()
                .productId("PROD-001")
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        CartLine line2 = CartLine.builder()
                .productId("PROD-002")
                .quantity(1)
                .productPrice(75.0)
                .totalPrice(75.0)
                .build();

        // Crear carrito de prueba
        testCart = Cart.builder()
                .id(new ObjectId())
                .userId(1L)
                .client(testClient)
                .cartLines(new ArrayList<>(List.of(line1, line2)))
                .totalItems(3)
                .total(175.0)
                .purchased(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void constructor_whenDependenciesProvided_createsOrderEmailService() {
        // Given
        EmailService mockEmailService = mock(EmailService.class);

        // When
        OrderEmailServiceImpl service = new OrderEmailServiceImpl(mockEmailService);

        // Then
        assertThat(service).isNotNull();
    }

    @Test
    void enviarConfirmacionPedido_whenValidCart_sendsSimpleHtmlEmail() {
        // Given
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        orderEmailService.enviarConfirmacionPedido(testCart);

        // Then
        verify(emailService, times(1)).sendHtmlEmail(
                toCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture());

        assertThat(toCaptor.getValue()).isEqualTo("juan.perez@example.com");
        assertThat(subjectCaptor.getValue()).contains("Confirmación de tu pedido");
        assertThat(subjectCaptor.getValue()).contains(testCart.getId().toString());

        String htmlBody = bodyCaptor.getValue();
        assertThat(htmlBody).contains("Juan Pérez");
        assertThat(htmlBody).contains("juan.perez@example.com");
        assertThat(htmlBody).contains("PROD-001");
        assertThat(htmlBody).contains("PROD-002");
    }

    @Test
    void enviarConfirmacionPedido_whenEmailServiceThrowsException_logsErrorAndDoesNotThrow() {
        // Given
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        // When & Then - no exception should be thrown
        orderEmailService.enviarConfirmacionPedido(testCart);

        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    void enviarConfirmacionPedidoHtml_whenValidCart_sendsCompleteHtmlEmail() {
        // Given
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        orderEmailService.enviarConfirmacionPedidoHtml(testCart);

        // Then
        verify(emailService, times(1)).sendHtmlEmail(
                toCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture());

        assertThat(toCaptor.getValue()).isEqualTo("juan.perez@example.com");
        assertThat(subjectCaptor.getValue()).contains("✅ Confirmación de tu pedido");
        assertThat(subjectCaptor.getValue()).contains(testCart.getId().toString());

        String htmlBody = bodyCaptor.getValue();
        assertThat(htmlBody).contains("Juan Pérez");
        assertThat(htmlBody).contains("juan.perez@example.com");
        assertThat(htmlBody).contains("Calle Principal");
        assertThat(htmlBody).contains("Madrid");
    }

    @Test
    void enviarConfirmacionPedidoHtml_whenEmailServiceThrowsException_logsErrorAndDoesNotThrow() {
        // Given
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        // When & Then - no exception should be thrown
        orderEmailService.enviarConfirmacionPedidoHtml(testCart);

        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    void enviarConfirmacionPedido_verifyHtmlBodyContainsAllRequiredInformation() {
        // Given
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        orderEmailService.enviarConfirmacionPedido(testCart);

        // Then
        verify(emailService).sendHtmlEmail(anyString(), anyString(), bodyCaptor.capture());

        String htmlBody = bodyCaptor.getValue();

        // Verificar información del cliente
        assertThat(htmlBody).contains(testClient.getName());
        assertThat(htmlBody).contains(testClient.getEmail());
        assertThat(htmlBody).contains(testClient.getPhone());

        // Verificar dirección
        assertThat(htmlBody).contains(testAddress.getStreet());
        assertThat(htmlBody).contains(String.valueOf(testAddress.getNumber()));
        assertThat(htmlBody).contains(testAddress.getCity());
        assertThat(htmlBody).contains(testAddress.getProvince());
        assertThat(htmlBody).contains(testAddress.getCountry());

        // Verificar estructura HTML
        assertThat(htmlBody).contains("<!DOCTYPE html>");
        assertThat(htmlBody).contains("</html>");
        assertThat(htmlBody).contains("Confirmación de Pedido");
    }

    @Test
    void enviarConfirmacionPedido_verifyNoOtherInteractions() {
        // When
        orderEmailService.enviarConfirmacionPedido(testCart);

        // Then
        verify(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void enviarConfirmacionPedidoHtml_verifyNoOtherInteractions() {
        // When
        orderEmailService.enviarConfirmacionPedidoHtml(testCart);

        // Then
        verify(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void enviarConfirmacionPedido_whenMultipleCartLines_includesAllProducts() {
        // Given - testCart already has 2 cart lines
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        orderEmailService.enviarConfirmacionPedido(testCart);

        // Then
        verify(emailService).sendHtmlEmail(anyString(), anyString(), bodyCaptor.capture());

        String htmlBody = bodyCaptor.getValue();
        assertThat(htmlBody).contains("PROD-001");
        assertThat(htmlBody).contains("PROD-002");
        assertThat(htmlBody).contains("Total de artículos: 3");
    }
}
