package dev.luisvives.dawazon.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitario para IdGenerator siguiendo principios FIRST.
 * <p>
 * - Fast: Tests rápidos sin dependencias externas
 * - Independent: Cada test es independiente y no depende de otros
 * - Repeatable: Se pueden ejecutar múltiples veces con los mismos resultados
 * - Self-validating: Cada test tiene una validación clara
 * - Timely: Tests escritos junto con el código de producción
 * </p>
 */
class IdGeneratorTest {

    private IdGenerator idGenerator;

    private static final String VALID_CHARS = "QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890-_";

    @BeforeEach
    void setUp() {
        idGenerator = new IdGenerator();
    }

    @Test
    void generate_whenCalled_returnsNonNullId() {
        // When
        Object generatedId = idGenerator.generate(null, null);

        // Then
        assertThat(generatedId).isNotNull();
        assertThat(generatedId).isInstanceOf(String.class);
    }

    @Test
    void generate_whenLengthNotSet_generatesIdWithDefaultLength12() {
        // When
        String generatedId = (String) idGenerator.generate(null, null);

        // Then
        assertThat(generatedId).hasSize(12);
    }

    @Test
    void generate_whenCustomLengthSet_generatesIdWithCustomLength() {
        // Given
        ReflectionTestUtils.setField(idGenerator, "length", 20);

        // When
        String generatedId = (String) idGenerator.generate(null, null);

        // Then
        assertThat(generatedId).hasSize(20);
    }

    @Test
    void generate_whenMultipleCalls_generatesUniqueIds() {
        // Given
        Set<String> generatedIds = new HashSet<>();
        int iterations = 100;

        // When
        for (int i = 0; i < iterations; i++) {
            String id = (String) idGenerator.generate(null, null);
            generatedIds.add(id);
        }

        // Then - Con 100 IDs aleatorios de 12 caracteres, la probabilidad de colisión
        // es extremadamente baja
        // Esperamos tener al menos 95 IDs únicos (permitiendo algunas colisiones raras)
        assertThat(generatedIds).hasSizeGreaterThan(95);
    }

    @Test
    void generate_whenCalled_generatesIdWithOnlyValidCharacters() {
        // When
        String generatedId = (String) idGenerator.generate(null, null);

        // Then - escapar el guion para que no se interprete como rango
        assertThat(generatedId).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]+");

        // Verificar cada caracter individualmente
        for (char c : generatedId.toCharArray()) {
            assertThat(VALID_CHARS).contains(String.valueOf(c));
        }
    }

    @Test
    void generate_whenLength5Set_generatesIdWithLength5() {
        // Given
        ReflectionTestUtils.setField(idGenerator, "length", 5);

        // When
        String generatedId = (String) idGenerator.generate(null, null);

        // Then
        assertThat(generatedId).hasSize(5);
        assertThat(generatedId).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]{5}");
    }

    @Test
    void generate_whenLength1Set_generatesIdWithLength1() {
        // Given
        ReflectionTestUtils.setField(idGenerator, "length", 1);

        // When
        String generatedId = (String) idGenerator.generate(null, null);

        // Then
        assertThat(generatedId).hasSize(1);
        assertThat(VALID_CHARS).contains(generatedId);
    }

    @Test
    void generate_whenCalledMultipleTimes_generatesNonEmptyIds() {
        // When & Then
        for (int i = 0; i < 10; i++) {
            String generatedId = (String) idGenerator.generate(null, null);
            assertThat(generatedId).isNotEmpty();
            assertThat(generatedId).hasSize(12);
        }
    }

    @Test
    void generate_verifyIdFormatConsistency() {
        // Given
        ReflectionTestUtils.setField(idGenerator, "length", 15);

        // When
        String id1 = (String) idGenerator.generate(null, null);
        String id2 = (String) idGenerator.generate(null, null);
        String id3 = (String) idGenerator.generate(null, null);

        // Then - Todos deben tener el mismo formato
        assertThat(id1).hasSize(15);
        assertThat(id2).hasSize(15);
        assertThat(id3).hasSize(15);

        assertThat(id1).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]{15}");
        assertThat(id2).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]{15}");
        assertThat(id3).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]{15}");
    }
}
