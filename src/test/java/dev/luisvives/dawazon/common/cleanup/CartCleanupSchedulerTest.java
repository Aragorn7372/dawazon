package dev.luisvives.dawazon.common.cleanup;

import dev.luisvives.dawazon.cart.service.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitario para CartCleanupScheduler siguiendo principios FIRST.
 * <p>
 * - Fast: Tests rápidos usando solo mocks
 * - Independent: Cada test es independiente y no depende de otros
 * - Repeatable: Se pueden ejecutar múltiples veces con los mismos resultados
 * - Self-validating: Cada test tiene una validación clara
 * - Timely: Tests escritos junto con el código de producción
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CartCleanupSchedulerTest {

    @Mock
    private CartServiceImpl cartService;

    @InjectMocks
    private CartCleanupScheduler cartCleanupScheduler;

    @BeforeEach
    void setUp() {
        // No es necesario configurar datos de prueba ya que el scheduler
        // solo delega al servicio
    }

    @Test
    void constructor_whenCartServiceProvided_createsScheduler() {
        // Given
        CartServiceImpl mockService = mock(CartServiceImpl.class);

        // When
        CartCleanupScheduler scheduler = new CartCleanupScheduler(mockService);

        // Then
        assertThat(scheduler).isNotNull();
    }

    @Test
    void cleanupExpiredCheckoutsAlternative_whenInvoked_callsCartServiceCleanup() {
        // When
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();

        // Then
        verify(cartService, times(1)).cleanupExpiredCheckouts();
    }

    @Test
    void cleanupExpiredCheckoutsAlternative_whenInvokedMultipleTimes_callsServiceEachTime() {
        // When
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();

        // Then
        verify(cartService, times(3)).cleanupExpiredCheckouts();
    }

    @Test
    void cleanupExpiredCheckoutsAlternative_whenServiceThrowsException_exceptionPropagates() {
        // Given
        RuntimeException expectedException = new RuntimeException("Service error");
        doThrow(expectedException).when(cartService).cleanupExpiredCheckouts();

        // When & Then
        try {
            cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();
        } catch (RuntimeException e) {
            assertThat(e).isEqualTo(expectedException);
            assertThat(e.getMessage()).isEqualTo("Service error");
        }

        verify(cartService, times(1)).cleanupExpiredCheckouts();
    }

    @Test
    void cleanupExpiredCheckoutsAlternative_verifyNoOtherInteractions() {
        // When
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();

        // Then
        verify(cartService).cleanupExpiredCheckouts();
        verifyNoMoreInteractions(cartService);
    }
}
