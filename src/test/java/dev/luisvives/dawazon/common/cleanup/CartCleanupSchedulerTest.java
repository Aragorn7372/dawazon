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


@ExtendWith(MockitoExtension.class)
class CartCleanupSchedulerTest {

    @Mock
    private CartServiceImpl cartService;

    @InjectMocks
    private CartCleanupScheduler cartCleanupScheduler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void constructorWhenCartServiceProvidedCreatesScheduler() {
        CartServiceImpl mockService = mock(CartServiceImpl.class);

        CartCleanupScheduler scheduler = new CartCleanupScheduler(mockService);

        assertThat(scheduler).isNotNull();
    }

    @Test
    void cleanupExpiredCheckoutsAlternativeWhenInvokedCallsCartServiceCleanup() {
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();

        verify(cartService, times(1)).cleanupExpiredCheckouts();
    }

    @Test
    void cleanupExpiredCheckoutsAlternative_whenInvokedMultipleTimes_callsServiceEachTime() {
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();

        verify(cartService, times(3)).cleanupExpiredCheckouts();
    }

    @Test
    void cleanupExpiredCheckoutsAlternativeWhenServiceThrowsExceptionExceptionPropagates() {
        RuntimeException expectedException = new RuntimeException("Service error");
        doThrow(expectedException).when(cartService).cleanupExpiredCheckouts();

        try {
            cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();
        } catch (RuntimeException e) {
            assertThat(e).isEqualTo(expectedException);
            assertThat(e.getMessage()).isEqualTo("Service error");
        }

        verify(cartService, times(1)).cleanupExpiredCheckouts();
    }

    @Test
    void cleanupExpiredCheckoutsAlternativeVerifyNoOtherInteractions() {
        cartCleanupScheduler.cleanupExpiredCheckoutsAlternative();

        verify(cartService).cleanupExpiredCheckouts();
        verifyNoMoreInteractions(cartService);
    }
}
