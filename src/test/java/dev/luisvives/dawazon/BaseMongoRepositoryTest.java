package dev.luisvives.dawazon;

import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Clase base para tests de repositorio MongoDB con Testcontainers.
 */
@DataMongoTest
public abstract class BaseMongoRepositoryTest {

    @MockitoBean  // Como field, no como parámetro de la anotación de clase
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final dev.luisvives.dawazon.TestContainersConfig containers =
            dev.luisvives.dawazon.TestContainersConfig.getInstance();

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> containers.getMongoDbContainer().getReplicaSetUrl());
        registry.add("spring.data.mongodb.database", () -> "tienda_test");
    }
}