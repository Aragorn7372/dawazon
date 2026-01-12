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
    void constructorwhenPathProvidedcreatesStorageService() {
        String testPath = tempDir.toString();

        FileSystemStorageService service = new FileSystemStorageService(testPath);

        assertThat(service).isNotNull();
    }

    @Test
    void initcreatesDirectoryStructure() {
        Path newTempDir = tempDir.resolve("test-storage");
        FileSystemStorageService newService = new FileSystemStorageService(newTempDir.toString());

        newService.init();

        assertThat(Files.exists(newTempDir)).isTrue();
        assertThat(Files.isDirectory(newTempDir)).isTrue();
    }

    @Test
    void storewhenValidFilestoresSuccessfully() throws IOException {
        String originalFilename = "test-image.jpg";
        byte[] content = "Test file content".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file",
                originalFilename,
                "image/jpeg",
                content);

        String storedFilename = storageService.store(file);

        assertThat(storedFilename).isNotNull();
        assertThat(storedFilename).contains("test-image");
        assertThat(storedFilename).endsWith(".jpg");

        Path storedFile = tempDir.resolve(storedFilename);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).isEqualTo(content);
    }

    @Test
    void storewhenEmptyFilethrowsStorageBadRequest() {
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]);

        assertThatThrownBy(() -> storageService.store(emptyFile))
                .isInstanceOf(StorageBadRequest.class)
                .hasMessageContaining("Fichero vacío");
    }

    @Test
    void storewhenFileContainsDoubleDotthrowsStorageBadRequest() {
        MultipartFile maliciousFile = new MockMultipartFile(
                "file",
                "../malicious.txt",
                "text/plain",
                "content".getBytes());

        assertThatThrownBy(() -> storageService.store(maliciousFile))
                .isInstanceOf(StorageBadRequest.class)
                .hasMessageContaining("ruta relativa");
    }

    @Test
    void loadwhenValidFilenamereturnsCorrectPath() {
        String filename = "test-file.txt";

        Path loadedPath = storageService.load(filename);

        assertThat(loadedPath).isNotNull();
        assertThat(loadedPath.getFileName().toString()).isEqualTo(filename);
        assertThat(loadedPath.getParent()).isEqualTo(tempDir);
    }

    @Test
    void loadAsResourcewhenFileExistsreturnsResource() throws IOException {
        String filename = "existing-file.txt";
        byte[] content = "Test content".getBytes();
        Files.write(tempDir.resolve(filename), content);

        Resource resource = storageService.loadAsResource(filename);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void loadAsResourcewhenFileNotFoundthrowsStorageNotFound() {
        String nonExistentFile = "non-existent.txt";

        assertThatThrownBy(() -> storageService.loadAsResource(nonExistentFile))
                .isInstanceOf(StorageNotFound.class)
                .hasMessageContaining("No se puede leer fichero");
    }

    @Test
    void deletewhenFileExistsdeletesSuccessfully() throws IOException {
        String filename = "file-to-delete.txt";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "content".getBytes());
        assertThat(Files.exists(filePath)).isTrue();

        storageService.delete(filename);

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void deletewhenFileDoesNotExistdoesNotThrowException() {
        String nonExistentFile = "non-existent.txt";

        storageService.delete(nonExistentFile);
    }

    @Test
    void deleteAllremovesAllFiles() throws IOException {
        Files.write(tempDir.resolve("file1.txt"), "content1".getBytes());
        Files.write(tempDir.resolve("file2.txt"), "content2".getBytes());
        assertThat(Files.exists(tempDir)).isTrue();

        storageService.deleteAll();

        assertThat(Files.exists(tempDir)).isFalse();
    }

    @Test
    void loadAllwhenMultipleFilesreturnsAllPaths() throws IOException {
        Files.write(tempDir.resolve("file1.txt"), "content1".getBytes());
        Files.write(tempDir.resolve("file2.txt"), "content2".getBytes());
        Files.write(tempDir.resolve("file3.jpg"), "content3".getBytes());

        List<Path> allFiles = storageService.loadAll().collect(Collectors.toList());

        assertThat(allFiles).hasSize(3);
        assertThat(allFiles).extracting(Path::getFileName)
                .extracting(Path::toString)
                .containsExactlyInAnyOrder("file1.txt", "file2.txt", "file3.jpg");
    }

    @Test
    void loadAllwhenEmptyDirectoryreturnsEmptyStream() {
        List<Path> allFiles = storageService.loadAll().collect(Collectors.toList());

        assertThat(allFiles).isEmpty();
    }

    @Test
    void storewhenMultipleFiles_storesAllWithUniqueNames() throws IOException, InterruptedException {
        MultipartFile file1 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content2".getBytes());

        String stored1 = storageService.store(file1);
        Thread.sleep(10); // Pequeña pausa para asegurar timestamp diferente
        String stored2 = storageService.store(file2);

        assertThat(stored1).isNotEqualTo(stored2); // Nombres deben ser diferentes
        assertThat(Files.exists(tempDir.resolve(stored1))).isTrue();
        assertThat(Files.exists(tempDir.resolve(stored2))).isTrue();
    }

    @Test
    void storewhenMultipartFileThrowsIOExceptionthrowsStorageInternal() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getInputStream()).thenThrow(new IOException("Simulated IO error"));

        assertThatThrownBy(() -> storageService.store(mockFile))
                .isInstanceOf(StorageInternal.class)
                .hasMessageContaining("Fallo al almacenar fichero");

        verify(mockFile).getInputStream();
    }

    @Test
    void initwhenCannotCreateDirectorythrowsStorageInternal() throws IOException {
        Path invalidPath = tempDir.resolve("file.txt");
        Files.write(invalidPath, "content".getBytes());

        Path conflictPath = invalidPath.resolve("subdir");
        FileSystemStorageService conflictService = new FileSystemStorageService(conflictPath.toString());
        assertThatThrownBy(() -> conflictService.init())
                .isInstanceOf(StorageInternal.class)
                .hasMessageContaining("No se puede inicializar el almacenamiento");
    }

    @Test
    void loadAllwhenDirectoryBecomesInvalidthrowsStorageInternal() {
        storageService.deleteAll();

        assertThatThrownBy(() -> storageService.loadAll().collect(Collectors.toList()))
                .isInstanceOf(StorageInternal.class)
                .hasMessageContaining("Fallo al leer ficheros almacenados");
    }

    @Test
    void deletewhenCannotDeleteFilethrowsStorageInternal() throws IOException {
        String filename = "readonly-file.txt";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "content".getBytes());


        String problematicPath = "../../../etc/passwd";

        storageService.delete(problematicPath);

    }
}
