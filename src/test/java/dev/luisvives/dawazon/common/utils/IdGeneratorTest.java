package dev.luisvives.dawazon.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class IdGeneratorTest {

    private IdGenerator idGenerator;

    private static final String VALID_CHARS = "QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890-_";

    @BeforeEach
    void setUp() {
        idGenerator = new IdGenerator();
    }

    @Test
    void generatewhenCalledreturnsNonNullId() {
        Object generatedId = idGenerator.generate(null, null);

        assertThat(generatedId).isNotNull();
        assertThat(generatedId).isInstanceOf(String.class);
    }

    @Test
    void generatewhenLengthNotSetgeneratesIdWithDefaultLength12() {
        String generatedId = (String) idGenerator.generate(null, null);

        assertThat(generatedId).hasSize(12);
    }

    @Test
    void generatewhenCustomLengthSetgeneratesIdWithCustomLength() {
        ReflectionTestUtils.setField(idGenerator, "length", 20);

        String generatedId = (String) idGenerator.generate(null, null);

        assertThat(generatedId).hasSize(20);
    }

    @Test
    void generatewhenMultipleCallsgeneratesUniqueIds() {
        Set<String> generatedIds = new HashSet<>();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            String id = (String) idGenerator.generate(null, null);
            generatedIds.add(id);
        }

        assertThat(generatedIds).hasSizeGreaterThan(95);
    }

    @Test
    void generatewhenCalledgeneratesIdWithOnlyValidCharacters() {
        String generatedId = (String) idGenerator.generate(null, null);

        assertThat(generatedId).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]+");

        for (char c : generatedId.toCharArray()) {
            assertThat(VALID_CHARS).contains(String.valueOf(c));
        }
    }

    @Test
    void generatewhenLength5SetgeneratesIdWithLength5() {
        ReflectionTestUtils.setField(idGenerator, "length", 5);

        String generatedId = (String) idGenerator.generate(null, null);

        assertThat(generatedId).hasSize(5);
        assertThat(generatedId).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]{5}");
    }

    @Test
    void generatewhenLength1SetgeneratesIdWithLength1() {
        ReflectionTestUtils.setField(idGenerator, "length", 1);

        String generatedId = (String) idGenerator.generate(null, null);

        assertThat(generatedId).hasSize(1);
        assertThat(VALID_CHARS).contains(generatedId);
    }

    @Test
    void generatewhenCalledMultipleTimesgeneratesNonEmptyIds() {
        for (int i = 0; i < 10; i++) {
            String generatedId = (String) idGenerator.generate(null, null);
            assertThat(generatedId).isNotEmpty();
            assertThat(generatedId).hasSize(12);
        }
    }

    @Test
    void generateverifyIdFormatConsistency() {
        ReflectionTestUtils.setField(idGenerator, "length", 15);

        String id1 = (String) idGenerator.generate(null, null);
        String id2 = (String) idGenerator.generate(null, null);
        String id3 = (String) idGenerator.generate(null, null);

        assertThat(id1).hasSize(15);
        assertThat(id2).hasSize(15);
        assertThat(id3).hasSize(15);

        assertThat(id1).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]{15}");
        assertThat(id2).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]{15}");
        assertThat(id3).matches("[QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890_\\-]{15}");
    }
}
