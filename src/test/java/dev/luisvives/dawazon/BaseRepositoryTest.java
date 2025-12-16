package dev.luisvives.dawazon;


import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Clase base para tests de repositorio (JPA) con Testcontainers.
 * Usa un singleton para mantener los contenedores vivos.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseRepositoryTest {

    // Obtener la instancia singleton
    private static final TestContainersConfig containers = TestContainersConfig.getInstance();

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", () -> containers.getPostgresContainer().getJdbcUrl());
        registry.add("spring.datasource.username", () -> containers.getPostgresContainer().getUsername());
        registry.add("spring.datasource.password", () -> containers.getPostgresContainer().getPassword());
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // MongoDB
        registry.add("spring.data.mongodb.uri", () -> containers.getMongoDbContainer().getReplicaSetUrl());
        registry.add("spring.data.mongodb.database", () -> "tienda_test");

        // Redis
        registry.add("spring.redis.host", () -> containers.getRedisContainer().getHost());
        registry.add("spring.redis.port", () -> containers.getRedisContainer().getFirstMappedPort());
    }
}