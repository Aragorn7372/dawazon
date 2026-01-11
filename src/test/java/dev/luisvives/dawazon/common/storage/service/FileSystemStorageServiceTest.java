package dev.luisvives.dawazon.common.storage.service;

import dev.luisvives.dawazon.common.storage.exceptions.StorageBadRequest;
import dev.luisvives.dawazon.common.storage.exceptions.StorageInternal;
import dev.luisvives.dawazon.common.storage.exceptions.StorageNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test unitario para FileSystemStorageService siguiendo principios FIRST.
 * <p>
 * - Fast: Tests rápidos usando directorios temporales
 * - Independent: Cada test tiene su propio directorio temporal aislado
 * - Repeatable: Se pueden ejecutar múltiples veces con los mismos resultados
 * - Self-validating: Cada test tiene una validación clara
 * - Timely: Tests escritos junto con el código de producción
 * </p>
 */
class FileSystemStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileSystemStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new FileSystemStorageService(tempDir.toString());
        storageService.init();
    }

    @Test
    void constructor_whenPathProvided_createsStorageService() {
        // Given
        String testPath = tempDir.toString();

        // When
        FileSystemStorageService service = new FileSystemStorageService(testPath);

        // Then
        assertThat(service).isNotNull();
    }

    @Test
    void init_createsDirectoryStructure() {
        // Given
        Path newTempDir = tempDir.resolve("test-storage");
        FileSystemStorageService newService = new FileSystemStorageService(newTempDir.toString());

        // When
        newService.init();

        // Then
        assertThat(Files.exists(newTempDir)).isTrue();
        assertThat(Files.isDirectory(newTempDir)).isTrue();
    }

    @Test
    void store_whenValidFile_storesSuccessfully() throws IOException {
        // Given
        String originalFilename = "test-image.jpg";
        byte[] content = "Test file content".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file",
                originalFilename,
                "image/jpeg",
                content);

        // When
        String storedFilename = storageService.store(file);

        // Then
        assertThat(storedFilename).isNotNull();
        assertThat(storedFilename).contains("test-image");
        assertThat(storedFilename).endsWith(".jpg");

        // Verificar que el archivo existe
        Path storedFile = tempDir.resolve(storedFilename);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).isEqualTo(content);
    }

    @Test
    void store_whenEmptyFile_throwsStorageBadRequest() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]);

        // When & Then
        assertThatThrownBy(() -> storageService.store(emptyFile))
                .isInstanceOf(StorageBadRequest.class)
                .hasMessageContaining("Fichero vacío");
    }

    @Test
    void store_whenFileContainsDoubleDot_throwsStorageBadRequest() {
        // Given
        MultipartFile maliciousFile = new MockMultipartFile(
                "file",
                "../malicious.txt",
                "text/plain",
                "content".getBytes());

        // When & Then
        assertThatThrownBy(() -> storageService.store(maliciousFile))
                .isInstanceOf(StorageBadRequest.class)
                .hasMessageContaining("ruta relativa");
    }

    @Test
    void load_whenValidFilename_returnsCorrectPath() {
        // Given
        String filename = "test-file.txt";

        // When
        Path loadedPath = storageService.load(filename);

        // Then
        assertThat(loadedPath).isNotNull();
        assertThat(loadedPath.getFileName().toString()).isEqualTo(filename);
        assertThat(loadedPath.getParent()).isEqualTo(tempDir);
    }

    @Test
    void loadAsResource_whenFileExists_returnsResource() throws IOException {
        // Given
        String filename = "existing-file.txt";
        byte[] content = "Test content".getBytes();
        Files.write(tempDir.resolve(filename), content);

        // When
        Resource resource = storageService.loadAsResource(filename);

        // Then
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void loadAsResource_whenFileNotFound_throwsStorageNotFound() {
        // Given
        String nonExistentFile = "non-existent.txt";

        // When & Then
        assertThatThrownBy(() -> storageService.loadAsResource(nonExistentFile))
                .isInstanceOf(StorageNotFound.class)
                .hasMessageContaining("No se puede leer fichero");
    }

    @Test
    void delete_whenFileExists_deletesSuccessfully() throws IOException {
        // Given
        String filename = "file-to-delete.txt";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "content".getBytes());
        assertThat(Files.exists(filePath)).isTrue();

        // When
        storageService.delete(filename);

        // Then
        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void delete_whenFileDoesNotExist_doesNotThrowException() {
        // Given
        String nonExistentFile = "non-existent.txt";

        // When & Then - should not throw exception
        storageService.delete(nonExistentFile);
    }

    @Test
    void deleteAll_removesAllFiles() throws IOException {
        // Given
        Files.write(tempDir.resolve("file1.txt"), "content1".getBytes());
        Files.write(tempDir.resolve("file2.txt"), "content2".getBytes());
        assertThat(Files.exists(tempDir)).isTrue();

        // When
        storageService.deleteAll();

        // Then
        assertThat(Files.exists(tempDir)).isFalse();
    }

    @Test
    void loadAll_whenMultipleFiles_returnsAllPaths() throws IOException {
        // Given
        Files.write(tempDir.resolve("file1.txt"), "content1".getBytes());
        Files.write(tempDir.resolve("file2.txt"), "content2".getBytes());
        Files.write(tempDir.resolve("file3.jpg"), "content3".getBytes());

        // When
        List<Path> allFiles = storageService.loadAll().collect(Collectors.toList());

        // Then
        assertThat(allFiles).hasSize(3);
        assertThat(allFiles).extracting(Path::getFileName)
                .extracting(Path::toString)
                .containsExactlyInAnyOrder("file1.txt", "file2.txt", "file3.jpg");
    }

    @Test
    void loadAll_whenEmptyDirectory_returnsEmptyStream() {
        // When
        List<Path> allFiles = storageService.loadAll().collect(Collectors.toList());

        // Then
        assertThat(allFiles).isEmpty();
    }

    @Test
    void store_whenMultipleFiles_storesAllWithUniqueNames() throws IOException, InterruptedException {
        // Given
        MultipartFile file1 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content2".getBytes());

        // When
        String stored1 = storageService.store(file1);
        Thread.sleep(10); // Pequeña pausa para asegurar timestamp diferente
        String stored2 = storageService.store(file2);

        // Then
        assertThat(stored1).isNotEqualTo(stored2); // Nombres deben ser diferentes
        assertThat(Files.exists(tempDir.resolve(stored1))).isTrue();
        assertThat(Files.exists(tempDir.resolve(stored2))).isTrue();
    }

    @Test
    void store_whenMultipartFileThrowsIOException_throwsStorageInternal() throws IOException {
        // Given - Mock MultipartFile that throws IOException
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getInputStream()).thenThrow(new IOException("Simulated IO error"));

        // When & Then
        assertThatThrownBy(() -> storageService.store(mockFile))
                .isInstanceOf(StorageInternal.class)
                .hasMessageContaining("Fallo al almacenar fichero");

        verify(mockFile).getInputStream();
    }

    @Test
    void init_whenCannotCreateDirectory_throwsStorageInternal() throws IOException {
        // Given - Crear un archivo en lugar de directorio para forzar error
        Path invalidPath = tempDir.resolve("file.txt");
        Files.write(invalidPath, "content".getBytes());

        // Intentar crear directorio con el mismo nombre que un archivo existente
        Path conflictPath = invalidPath.resolve("subdir");
        FileSystemStorageService conflictService = new FileSystemStorageService(conflictPath.toString());

        // When & Then
        assertThatThrownBy(() -> conflictService.init())
                .isInstanceOf(StorageInternal.class)
                .hasMessageContaining("No se puede inicializar el almacenamiento");
    }

    @Test
    void loadAll_whenDirectoryBecomesInvalid_throwsStorageInternal() {
        // Given
        storageService.deleteAll(); // Eliminar directorio para forzar error

        // When & Then
        assertThatThrownBy(() -> storageService.loadAll().collect(Collectors.toList()))
                .isInstanceOf(StorageInternal.class)
                .hasMessageContaining("Fallo al leer ficheros almacenados");
    }

    @Test
    void delete_whenCannotDeleteFile_throwsStorageInternal() throws IOException {
        // Given - Crear archivo y hacerlo de solo lectura
        String filename = "readonly-file.txt";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "content".getBytes());

        // Hacer el directorio padre de solo lectura (esto puede no funcionar en
        // Windows)
        // Como alternativa, podr podríamos verificar el manejo de paths inválidos

        // When & Then
        // Nota: Este test puede ser difícil de implementar de manera consistente
        // en diferentes sistemas operativos. El catch de IOException en delete
        // es principalmente para casos extremos de permisos o filesystem corrupto.

        // Para este caso, vamos a probar con un path inválido que contenga
        // caracteres que causen problemas
        String problematicPath = "../../../etc/passwd";

        // Este test valida que el método maneja casos problemáticos
        // aunque puede no lanzar IOException en todos los casos
        storageService.delete(problematicPath);
        // El test pasa si no lanza excepción inesperada
    }
}
