package dev.luisvives.dawazon.products.service;

import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.products.mapper.ProductMapper;
import dev.luisvives.dawazon.products.models.Category;
import dev.luisvives.dawazon.products.models.Comment;
import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.products.repository.CategoryRepository;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private StorageService storageService;

        @Mock
        private ProductMapper productMapper;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private ProductServiceImpl productService;

        private Product testProduct;
        private Category testCategory;
        private PostProductRequestDto testProductDto;
        private GenericProductResponseDto testResponseDto;
        private User testUser;

        @BeforeEach
        void setUp() {
                testCategory = new Category();
                testCategory.setId("cat-1");
                testCategory.setName("Electronics");
                testCategory.setCreatedAt(LocalDateTime.now());
                testCategory.setUpdatedAt(LocalDateTime.now());

                testUser = User.builder()
                                .id(1L)
                                .userName("testuser")
                                .build();

                testProduct = Product.builder()
                                .id("test-id-123")
                                .name("Test Product")
                                .description("Test Description")
                                .price(99.99)
                                .stock(10)
                                .category(testCategory)
                                .creatorId(1L)
                                .images(new ArrayList<>(List.of("image1.jpg", "image2.jpg")))
                                .comments(new ArrayList<>())
                                .isDeleted(false)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                testProductDto = PostProductRequestDto.builder()
                                .name("Test Product")
                                .description("Test Description")
                                .price(99.99)
                                .stock(10)
                                .category("Electronics")
                                .creatorId(1L)
                                .build();

                testResponseDto = GenericProductResponseDto.builder()
                                .id("test-id-123")
                                .name("Test Product")
                                .description("Test Description")
                                .price(99.99)
                                .stock(10)
                                .category("Electronics")
                                .image(List.of("image1.jpg", "image2.jpg"))
                                .comments(new ArrayList<>())
                                .build();
        }

        @Test
        void findAlldebeRetornarPaginaDeProductosCuandoNoHayFiltros() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Product> expectedPage = new PageImpl<>(List.of(testProduct));

                when(productRepository.findAll(any(Specification.class), eq(pageable)))
                                .thenReturn(expectedPage);

                Page<Product> result = productService.findAll(
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                pageable);

                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                assertEquals(testProduct, result.getContent().get(0));
                verify(productRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void findAlldebeRetornarPaginaDeProductosCuandoSeFiltrarPorNombre() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Product> expectedPage = new PageImpl<>(List.of(testProduct));

                when(productRepository.findAll(any(Specification.class), eq(pageable)))
                                .thenReturn(expectedPage);

                Page<Product> result = productService.findAll(
                                Optional.of("Test"),
                                Optional.empty(),
                                Optional.empty(),
                                pageable);

                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                verify(productRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void findAlldebeRetornarPaginaDeProductosCuandoSeFiltrarPorCategoria() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Product> expectedPage = new PageImpl<>(List.of(testProduct));

                when(productRepository.findAll(any(Specification.class), eq(pageable)))
                                .thenReturn(expectedPage);

                Page<Product> result = productService.findAll(
                                Optional.empty(),
                                Optional.of("Electronics"),
                                Optional.empty(),
                                pageable);

                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                verify(productRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void findAlldebeRetornarPaginaDeProductosCuandoSeFiltrarPorCreadorId() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Product> expectedPage = new PageImpl<>(List.of(testProduct));

                when(productRepository.findAll(any(Specification.class), eq(pageable)))
                                .thenReturn(expectedPage);

                Page<Product> result = productService.findAll(
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(1L),
                                pageable);

                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                verify(productRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void getByIddebeRetornarProductoCuandoExiste() {
                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(productMapper.modelToGenericResponseDTO(eq(testProduct), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.getById("test-id-123");

                assertNotNull(result);
                assertEquals("test-id-123", result.getId());
                assertEquals("Test Product", result.getName());
                verify(productRepository, times(1)).findById("test-id-123");
                verify(productMapper, times(1)).modelToGenericResponseDTO(eq(testProduct), anyList());
        }

        @Test
        void getByIddebeLanzarExcepcionCuandoProductoNoExiste() {
                when(productRepository.findById("non-existent-id")).thenReturn(Optional.empty());

                ProductException.NotFoundException exception = assertThrows(
                                ProductException.NotFoundException.class,
                                () -> productService.getById("non-existent-id"));

                assertTrue(exception.getMessage().contains("non-existent-id"));
                verify(productRepository, times(1)).findById("non-existent-id");
                verify(productMapper, never()).modelToGenericResponseDTO(any(), anyList());
        }

        @Test
        void getUserProductIddebeRetornarIdDelCreadorCuandoExiste() {
                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));

                Long result = productService.getUserProductId("test-id-123");

                assertNotNull(result);
                assertEquals(1L, result);
                verify(productRepository, times(1)).findById("test-id-123");
        }

        @Test
        void getUserProductIddebeLanzarExcepcionCuandoProductoNoExiste() {
                when(productRepository.findById("non-existent-id")).thenReturn(Optional.empty());

                ProductException.NotFoundException exception = assertThrows(
                                ProductException.NotFoundException.class,
                                () -> productService.getUserProductId("non-existent-id"));

                assertTrue(exception.getMessage().contains("non-existent-id"));
                verify(productRepository, times(1)).findById("non-existent-id");
        }

        @Test
        void savedebeGuardarProductoCuandoCategoriaExiste() {
                when(categoryRepository.findByNameIgnoreCase("Electronics"))
                                .thenReturn(Optional.of(testCategory));
                when(productMapper.postPutDTOToModel(testProductDto)).thenReturn(testProduct);
                when(productRepository.save(any(Product.class))).thenReturn(testProduct);
                when(productMapper.modelToGenericResponseDTO(eq(testProduct), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.save(testProductDto);

                assertNotNull(result);
                assertEquals("test-id-123", result.getId());
                assertEquals("Test Product", result.getName());
                verify(categoryRepository, times(1)).findByNameIgnoreCase("Electronics");
                verify(productMapper, times(1)).postPutDTOToModel(testProductDto);
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void savedebeLanzarExcepcionCuandoCategoriaNoExiste() {
                when(categoryRepository.findByNameIgnoreCase("NonExistentCategory"))
                                .thenReturn(Optional.empty());

                testProductDto.setCategory("NonExistentCategory");

                ProductException.ValidationException exception = assertThrows(
                                ProductException.ValidationException.class,
                                () -> productService.save(testProductDto));

                assertTrue(exception.getMessage().contains("NonExistentCategory"));
                verify(categoryRepository, times(1)).findByNameIgnoreCase("NonExistentCategory");
                verify(productRepository, never()).save(any());
        }

        @Test
        void updatedebeActualizarProductoCuandoExisteYCategoriaEsValida() {
                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(categoryRepository.findByNameIgnoreCase("Electronics"))
                                .thenReturn(Optional.of(testCategory));
                when(productRepository.save(any(Product.class))).thenReturn(testProduct);
                when(productMapper.modelToGenericResponseDTO(eq(testProduct), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.update("test-id-123", testProductDto);

                assertNotNull(result);
                verify(productRepository, times(1)).findById("test-id-123");
                verify(categoryRepository, times(1)).findByNameIgnoreCase("Electronics");
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void updatedebeLanzarExcepcionCuandoProductoNoExiste() {
                when(productRepository.findById("non-existent-id")).thenReturn(Optional.empty());

                ProductException.NotFoundException exception = assertThrows(
                                ProductException.NotFoundException.class,
                                () -> productService.update("non-existent-id", testProductDto));

                assertTrue(exception.getMessage().contains("non-existent-id"));
                verify(productRepository, times(1)).findById("non-existent-id");
                verify(productRepository, never()).save(any());
        }

        @Test
        void updatedebeLanzarExcepcionCuandoCategoriaNoExiste() {
                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(categoryRepository.findByNameIgnoreCase("NonExistentCategory"))
                                .thenReturn(Optional.empty());

                testProductDto.setCategory("NonExistentCategory");

                ProductException.ValidationException exception = assertThrows(
                                ProductException.ValidationException.class,
                                () -> productService.update("test-id-123", testProductDto));

                assertTrue(exception.getMessage().contains("NonExistentCategory"));
                verify(productRepository, times(1)).findById("test-id-123");
                verify(categoryRepository, times(1)).findByNameIgnoreCase("NonExistentCategory");
                verify(productRepository, never()).save(any());
        }

        @Test
        void getAllCategoriasdebeRetornarListaDeNombresDeCategorias() {
                Category cat1 = new Category();
                cat1.setName("Electronics");
                Category cat2 = new Category();
                cat2.setName("Books");
                Category cat3 = new Category();
                cat3.setName("Clothing");
                List<Category> categories = List.of(cat1, cat2, cat3);
                when(categoryRepository.findAll()).thenReturn(categories);

                List<String> result = productService.getAllCategorias();

                assertNotNull(result);
                assertEquals(3, result.size());
                assertTrue(result.contains("Electronics"));
                assertTrue(result.contains("Books"));
                assertTrue(result.contains("Clothing"));
                verify(categoryRepository, times(1)).findAll();
        }

        @Test
        void deleteByIddebeEliminarProductoCuandoExiste() {
                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                doNothing().when(productRepository).deleteByIdLogical("test-id-123");

                assertDoesNotThrow(() -> productService.deleteById("test-id-123"));

                verify(productRepository, times(1)).findById("test-id-123");
                verify(productRepository, times(1)).deleteByIdLogical("test-id-123");
        }

        @Test
        void deleteByIddebeLanzarExcepcionCuandoProductoNoExiste() {
                when(productRepository.findById("non-existent-id")).thenReturn(Optional.empty());

                ProductException.NotFoundException exception = assertThrows(
                                ProductException.NotFoundException.class,
                                () -> productService.deleteById("non-existent-id"));

                assertTrue(exception.getMessage().contains("non-existent-id"));
                verify(productRepository, times(1)).findById("non-existent-id");
                verify(productRepository, never()).deleteByIdLogical(any());
        }

        @Test
        void updateOrSaveImagedebeActualizarImagenesCuandoSonValidas() {
                MultipartFile mockFile1 = mock(MultipartFile.class);
                MultipartFile mockFile2 = mock(MultipartFile.class);
                List<MultipartFile> newImages = List.of(mockFile1, mockFile2);

                when(mockFile1.isEmpty()).thenReturn(false);
                when(mockFile2.isEmpty()).thenReturn(false);
                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(storageService.store(mockFile1)).thenReturn("new-image1.jpg");
                when(storageService.store(mockFile2)).thenReturn("new-image2.jpg");
                when(productRepository.save(any(Product.class))).thenReturn(testProduct);
                when(productMapper.modelToGenericResponseDTO(eq(testProduct), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.updateOrSaveImage("test-id-123", newImages);

                assertNotNull(result);
                verify(productRepository, times(1)).findById("test-id-123");
                verify(storageService, times(2)).delete(anyString());
                verify(storageService, times(1)).store(mockFile1);
                verify(storageService, times(1)).store(mockFile2);
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void updateOrSaveImagedebeLanzarExcepcionCuandoProductoNoExiste() {
                MultipartFile mockFile = mock(MultipartFile.class);
                List<MultipartFile> newImages = List.of(mockFile);

                when(productRepository.findById("non-existent-id")).thenReturn(Optional.empty());

                ProductException.NotFoundException exception = assertThrows(
                                ProductException.NotFoundException.class,
                                () -> productService.updateOrSaveImage("non-existent-id", newImages));

                assertTrue(exception.getMessage().contains("non-existent-id"));
                verify(productRepository, times(1)).findById("non-existent-id");
                verify(storageService, never()).store(any());
                verify(productRepository, never()).save(any());
        }

        @Test
        void updateOrSaveImagenoDebeModificarImagenesCuandoListaEstaVacia() {
                List<MultipartFile> emptyImages = new ArrayList<>();

                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(productMapper.modelToGenericResponseDTO(eq(testProduct), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.updateOrSaveImage("test-id-123", emptyImages);

                assertNotNull(result);
                verify(productRepository, times(1)).findById("test-id-123");
                verify(storageService, never()).delete(anyString());
                verify(storageService, never()).store(any());
                verify(productRepository, never()).save(any());
        }

        @Test
        void updateOrSaveImagedebeFiltrarArchivosVaciosYProcesarSoloValidos() {
                MultipartFile mockFileEmpty = mock(MultipartFile.class);
                MultipartFile mockFileValid = mock(MultipartFile.class);
                List<MultipartFile> mixedImages = List.of(mockFileEmpty, mockFileValid);

                when(mockFileEmpty.isEmpty()).thenReturn(true);
                when(mockFileValid.isEmpty()).thenReturn(false);
                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(storageService.store(mockFileValid)).thenReturn("new-valid-image.jpg");
                when(productRepository.save(any(Product.class))).thenReturn(testProduct);
                when(productMapper.modelToGenericResponseDTO(eq(testProduct), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.updateOrSaveImage("test-id-123", mixedImages);

                assertNotNull(result);
                verify(productRepository, times(1)).findById("test-id-123");
                verify(storageService, times(2)).delete(anyString());
                verify(storageService, times(1)).store(mockFileValid);
                verify(storageService, never()).store(mockFileEmpty);
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void findByCreatedAtBetweendebeRetornarProductosEnRangoDeFechas() {
                LocalDateTime start = LocalDateTime.now().minusDays(7);
                LocalDateTime end = LocalDateTime.now();
                List<Product> expectedProducts = List.of(testProduct);

                when(productRepository.findAllBycreatedAtBetween(start, end))
                                .thenReturn(expectedProducts);

                List<Product> result = productService.findByCreatedAtBetween(start, end);

                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(testProduct, result.get(0));
                verify(productRepository, times(1)).findAllBycreatedAtBetween(start, end);
        }

        @Test
        void addCommentdebeAgregarComentarioCuandoProductoExiste() {
                Comment testComment = Comment.builder()
                                .content("Great product!")
                                .userId(1L)
                                .verified(false)
                                .recommended(true)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(productRepository.save(any(Product.class))).thenReturn(testProduct);

                assertDoesNotThrow(() -> productService.addComment("test-id-123", testComment));

                verify(productRepository, times(1)).findById("test-id-123");
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void addCommentdebeLanzarExcepcionCuandoProductoNoExiste() {
                Comment testComment = Comment.builder()
                                .content("Great product!")
                                .userId(1L)
                                .verified(false)
                                .recommended(true)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(productRepository.findById("non-existent-id")).thenReturn(Optional.empty());

                ProductException.NotFoundException exception = assertThrows(
                                ProductException.NotFoundException.class,
                                () -> productService.addComment("non-existent-id", testComment));

                assertTrue(exception.getMessage().contains("non-existent-id"));
                verify(productRepository, times(1)).findById("non-existent-id");
                verify(productRepository, never()).save(any());
        }

        @Test
        void getByIddebeMappearComentariosCorrectamente() {
                Comment testComment = Comment.builder()
                                .content("Excelente producto")
                                .userId(1L)
                                .verified(true)
                                .recommended(true)
                                .createdAt(LocalDateTime.now())
                                .build();

                testProduct.getComments().add(testComment);

                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(productMapper.modelToGenericResponseDTO(eq(testProduct), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.getById("test-id-123");

                assertNotNull(result);
                verify(productRepository, times(1)).findById("test-id-123");
                verify(userRepository, times(1)).findById(1L);
                verify(productMapper, times(1)).modelToGenericResponseDTO(eq(testProduct), anyList());
        }

        @Test
        void updateOrSaveImagedebeManejarArchivosNullEnLaLista() {
                MultipartFile mockFileValid = mock(MultipartFile.class);
                List<MultipartFile> imagesWithNull = new ArrayList<>();
                imagesWithNull.add(null);
                imagesWithNull.add(mockFileValid);

                when(mockFileValid.isEmpty()).thenReturn(false);
                when(productRepository.findById("test-id-123")).thenReturn(Optional.of(testProduct));
                when(storageService.store(mockFileValid)).thenReturn("new-valid-image.jpg");
                when(productRepository.save(any(Product.class))).thenReturn(testProduct);
                when(productMapper.modelToGenericResponseDTO(eq(testProduct), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.updateOrSaveImage("test-id-123", imagesWithNull);

                assertNotNull(result);
                verify(productRepository, times(1)).findById("test-id-123");
                verify(storageService, times(2)).delete(anyString());
                verify(storageService, times(1)).store(mockFileValid);
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void updateOrSaveImagedebeManejarProductoConImagenesNull() {
                Product productWithoutImages = Product.builder()
                                .id("test-id-456")
                                .name("Product Without Images")
                                .description("Test Description")
                                .price(49.99)
                                .stock(5)
                                .category(testCategory)
                                .creatorId(1L)
                                .images(null)
                                .comments(new ArrayList<>())
                                .isDeleted(false)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                MultipartFile mockFile = mock(MultipartFile.class);
                List<MultipartFile> newImages = List.of(mockFile);

                when(mockFile.isEmpty()).thenReturn(false);
                when(productRepository.findById("test-id-456")).thenReturn(Optional.of(productWithoutImages));
                when(storageService.store(mockFile)).thenReturn("new-image.jpg");
                when(productRepository.save(any(Product.class))).thenReturn(productWithoutImages);
                when(productMapper.modelToGenericResponseDTO(eq(productWithoutImages), anyList()))
                                .thenReturn(testResponseDto);

                GenericProductResponseDto result = productService.updateOrSaveImage("test-id-456", newImages);

                assertNotNull(result);
                verify(productRepository, times(1)).findById("test-id-456");
                verify(storageService, never()).delete(anyString());
                verify(storageService, times(1)).store(mockFile);
                verify(productRepository, times(1)).save(any(Product.class));
        }
}
