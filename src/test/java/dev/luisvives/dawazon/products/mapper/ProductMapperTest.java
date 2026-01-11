package dev.luisvives.dawazon.products.mapper;

import dev.luisvives.dawazon.common.dto.PageResponseDTO;
import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.products.dto.CommentDto;
import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.models.Category;
import dev.luisvives.dawazon.products.models.Comment;
import dev.luisvives.dawazon.products.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitario para ProductMapper siguiendo principios FIRST.
 * <p>
 * - Fast: Tests rápidos usando solo mocks
 * - Independent: Cada test es independiente y no depende de otros
 * - Repeatable: Se pueden ejecutar múltiples veces con los mismos resultados
 * - Self-validating: Cada test tiene una validación clara
 * - Timely: Tests escritos junto con el código de producción
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ProductMapper productMapper;

    private Product testProduct;
    private Category testCategory;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        // Setup test category
        testCategory = new Category();
        testCategory.setId("CAT-001");
        testCategory.setName("Electronics");
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());

        // Setup test comment
        testComment = Comment.builder()
                .userId(100L)
                .content("Great product!")
                .verified(true)
                .recommended(true)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup test product
        testProduct = Product.builder()
                .id("PROD-001")
                .name("Laptop")
                .price(999.99)
                .stock(10)
                .description("High-performance laptop")
                .creatorId(1L)
                .images(List.of("image1.jpg", "image2.jpg"))
                .category(testCategory)
                .comments(new ArrayList<>(List.of(testComment)))
                .build();
    }

    @Test
    void constructor_whenStorageServiceProvided_createsProductMapper() {
        // Given
        StorageService mockStorageService = mock(StorageService.class);

        // When
        ProductMapper mapper = new ProductMapper(mockStorageService);

        // Then
        assertThat(mapper).isNotNull();
    }

    @Test
    void modelToGenericResponseDTO_whenValidProduct_convertsSuccessfully() {
        // Given
        when(storageService.getUrl("image1.jpg")).thenReturn("http://example.com/image1.jpg");
        when(storageService.getUrl("image2.jpg")).thenReturn("http://example.com/image2.jpg");

        CommentDto commentDto = CommentDto.builder()
                .comment("Great product!")
                .userName("User123")
                .verified(true)
                .recommended(true)
                .build();
        List<CommentDto> commentDtos = List.of(commentDto);

        // When
        GenericProductResponseDto result = productMapper.modelToGenericResponseDTO(testProduct, commentDtos);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("PROD-001");
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getPrice()).isEqualTo(999.99);
        assertThat(result.getStock()).isEqualTo(10);
        assertThat(result.getDescription()).isEqualTo("High-performance laptop");
        assertThat(result.getCategory()).isEqualTo("Electronics");
        assertThat(result.getImage()).containsExactly("http://example.com/image1.jpg", "http://example.com/image2.jpg");
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getComment()).isEqualTo("Great product!");

        verify(storageService).getUrl("image1.jpg");
        verify(storageService).getUrl("image2.jpg");
        verifyNoMoreInteractions(storageService);
    }

    @Test
    void modelToGenericResponseDTO_whenProductWithNoImages_returnsEmptyImageList() {
        // Given
        testProduct.setImages(new ArrayList<>());

        // When
        GenericProductResponseDto result = productMapper.modelToGenericResponseDTO(testProduct, List.of());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImage()).isEmpty();
        verifyNoInteractions(storageService);
    }

    @Test
    void postPutDTOToModel_whenValidDTO_convertsSuccessfully() {
        // Given
        PostProductRequestDto dto = PostProductRequestDto.builder()
                .id("PROD-002")
                .name("Mouse")
                .price(29.99)
                .stock(50)
                .description("Wireless mouse")
                .category("Electronics")
                .creatorId(2L)
                .build();

        // When
        Product result = productMapper.postPutDTOToModel(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("PROD-002");
        assertThat(result.getName()).isEqualTo("Mouse");
        assertThat(result.getPrice()).isEqualTo(29.99);
        assertThat(result.getStock()).isEqualTo(50);
        assertThat(result.getDescription()).isEqualTo("Wireless mouse");
        assertThat(result.getCreatorId()).isEqualTo(2L);
        assertThat(result.getImages()).isEmpty();
        assertThat(result.getComments()).isEmpty();
        assertThat(result.getCategory()).isNull(); // Category debe establecerse en el servicio
    }

    @Test
    void commentToCommentDto_whenValidComment_convertsSuccessfully() {
        // Given
        String userName = "John Doe";

        // When
        CommentDto result = productMapper.commentToCommentDto(testComment, userName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo("Great product!");
        assertThat(result.getUserName()).isEqualTo("John Doe");
        assertThat(result.isVerified()).isTrue();
        assertThat(result.isRecommended()).isTrue();
    }

    @Test
    void commentToCommentDto_whenNotVerifiedComment_convertsCorrectly() {
        // Given
        Comment unverifiedComment = Comment.builder()
                .userId(200L)
                .content("Average product")
                .verified(false)
                .recommended(false)
                .build();
        String userName = "Jane Smith";

        // When
        CommentDto result = productMapper.commentToCommentDto(unverifiedComment, userName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo("Average product");
        assertThat(result.getUserName()).isEqualTo("Jane Smith");
        assertThat(result.isVerified()).isFalse();
        assertThat(result.isRecommended()).isFalse();
    }

    @Test
    void pageToDTO_whenValidPage_convertsSuccessfully() {
        // Given
        List<Product> products = List.of(testProduct);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        String sortBy = "name";
        String direction = "asc";

        // When
        PageResponseDTO<Product> result = productMapper.pageToDTO(page, sortBy, direction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testProduct);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalPageElements()).isEqualTo(1);
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
        assertThat(result.getSortBy()).isEqualTo("name");
        assertThat(result.getDirection()).isEqualTo("asc");
    }

    @Test
    void pageToDTO_whenEmptyPage_returnsEmptyDTO() {
        // Given
        Page<Product> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);

        // When
        PageResponseDTO<Product> result = productMapper.pageToDTO(emptyPage, "price", "desc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void modelToGenericResponseDTO_whenMultipleComments_includesAllComments() {
        // Given
        when(storageService.getUrl(anyString())).thenReturn("http://example.com/image.jpg");

        CommentDto comment1 = CommentDto.builder().comment("Comment 1").userName("User1").build();
        CommentDto comment2 = CommentDto.builder().comment("Comment 2").userName("User2").build();
        List<CommentDto> commentDtos = List.of(comment1, comment2);

        // When
        GenericProductResponseDto result = productMapper.modelToGenericResponseDTO(testProduct, commentDtos);

        // Then
        assertThat(result.getComments()).hasSize(2);
        assertThat(result.getComments()).containsExactly(comment1, comment2);
    }

    @Test
    void postPutDTOToModel_verifiesImagesAndCommentsAreInitializedAsEmptyLists() {
        // Given
        PostProductRequestDto dto = PostProductRequestDto.builder()
                .id("PROD-003")
                .name("Keyboard")
                .price(79.99)
                .stock(25)
                .description("Mechanical keyboard")
                .category("Electronics")
                .creatorId(3L)
                .build();

        // When
        Product result = productMapper.postPutDTOToModel(dto);

        // Then
        assertThat(result.getImages()).isNotNull();
        assertThat(result.getImages()).isEmpty();
        assertThat(result.getComments()).isNotNull();
        assertThat(result.getComments()).isEmpty();
    }
}
