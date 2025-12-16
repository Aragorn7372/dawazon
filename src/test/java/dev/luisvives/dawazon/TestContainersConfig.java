package dev.luisvives.dawazon;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton para mantener los contenedores Testcontainers vivos durante toda la suite de tests.
 */
public class TestContainersConfig {

    private static TestContainersConfig instance;

    private final PostgreSQLContainer<?> postgresContainer;
    private final MongoDBContainer mongoDbContainer;
    private final GenericContainer<?> redisContainer;

    private TestContainersConfig() {
        // Inicializar PostgreSQL
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withReuse(true);
        postgresContainer.start();

        // Inicializar MongoDB
        mongoDbContainer = new MongoDBContainer("mongo:5.0")
                .withReuse(true);
        mongoDbContainer.start();

        // Inicializar Redis
        redisContainer = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379)
                .withReuse(true);
        redisContainer.start();

        // Registrar shutdown hook para cerrar contenedores al finalizar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            postgresContainer.stop();
            mongoDbContainer.stop();
            redisContainer.stop();
        }));
    }

    public static synchronized TestContainersConfig getInstance() {
        if (instance == null) {
            instance = new TestContainersConfig();
        }
        return instance;
    }

    public PostgreSQLContainer<?> getPostgresContainer() {
        return postgresContainer;
    }

    public MongoDBContainer getMongoDbContainer() {
        return mongoDbContainer;
    }

    public GenericContainer<?> getRedisContainer() {
        return redisContainer;
    }
}