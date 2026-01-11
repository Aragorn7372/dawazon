package dev.luisvives.dawazon.products.repository;

import dev.luisvives.dawazon.BaseRepositoryTest;
import dev.luisvives.dawazon.products.models.Category;
import dev.luisvives.dawazon.products.models.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ProductRepository using TestContainers.
 * <p>
 * Tests following FIRST principles:
 * - Fast: TestContainers reuses containers between tests
 * - Independent: Each test cleans up its data
 * - Repeatable: Uses isolated PostgreSQL container
 * - Self-validating: Clear assertions for all operations
 * - Timely: Tests repository functionality with real database
 * </p>
 */
class ProductRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void save_whenValidProduct_savesSuccessfully() {
        // Given
        Product product = createProductBuilder("Laptop", 999.99, 10, 1L).build();

        // When
        Product saved = productRepository.save(product);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Laptop");
        assertThat(saved.getPrice()).isEqualTo(999.99);
        assertThat(saved.getStock()).isEqualTo(10);
    }

    @Test
    void findById_whenProductExists_returnsProduct() {
        // Given
        Product product = createAndSaveProduct("Mouse", 29.99, 50, 1L);

        // When
        Optional<Product> found = productRepository.findById(product.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Mouse");
    }

    @Test
    void findById_whenProductDoesNotExist_returnsEmpty() {
        // When
        Optional<Product> found = productRepository.findById("NON_EXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAllBycreatedAtBetween_whenProductsInRange_returnsProducts() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusHours(1);
        createAndSaveProduct("Product1", 10.0, 5, 1L);
        createAndSaveProduct("Product2", 20.0, 3, 1L);
        LocalDateTime after = LocalDateTime.now().plusHours(1);

        // When
        List<Product> products = productRepository.findAllBycreatedAtBetween(before, after);

        // Then
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Product1", "Product2");
    }

    @Test
    void findAllBycreatedAtBetween_whenNoProductsInRange_returnsEmpty() {
        // Given
        createAndSaveProduct("OldProduct", 10.0, 5, 1L);
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(2);

        // When
        List<Product> products = productRepository.findAllBycreatedAtBetween(futureStart, futureEnd);

        // Then
        assertThat(products).isEmpty();
    }

    @Test
    void substractStock_whenSufficientStock_updatesSuccessfully() {
        // Given
        Product product = createAndSaveProduct("Product", 100.0, 10, 1L);
        String productId = product.getId();
        Long version = product.getVersion();

        // When
        int rowsAffected = productRepository.substractStock(productId, 5, version);
        productRepository.flush();
        entityManager.clear();
        // Then
        assertThat(rowsAffected).isEqualTo(1); // 1 row affected
        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(5);
    }

    @Test
    void substractStock_whenInsufficientStock_doesNotUpdate() {
        // Given
        Product product = createAndSaveProduct("Product", 100.0, 3, 1L);
        String productId = product.getId();
        Long version = product.getVersion();

        // When
        int rowsAffected = productRepository.substractStock(productId, 5, version);
        productRepository.flush();

        // Then - No rows affected, stock should remain unchanged
        assertThat(rowsAffected).isEqualTo(0); // 0 rows affected
        Product unchanged = productRepository.findById(productId).orElseThrow();
        assertThat(unchanged.getStock()).isEqualTo(3);
    }

    @Test
    void substractStock_whenProductDeleted_doesNotUpdate() {
        // Given
        Product product = createAndSaveProduct("Product", 100.0, 10, 1L);
        String productId = product.getId();

        // Mark product as deleted
        productRepository.deleteByIdLogical(productId);
        productRepository.flush();
        entityManager.clear();

        // Get updated product with new version after logical delete
        Product deletedProduct = productRepository.findById(productId).orElseThrow();
        Long newVersion = deletedProduct.getVersion();

        // When - Try to subtract stock with new version
        int rowsAffected = productRepository.substractStock(productId, 5, newVersion);
        productRepository.flush();

        // Then - No rows affected because product is deleted
        assertThat(rowsAffected).isEqualTo(0); // 0 rows affected
        entityManager.clear();
        Product stillDeleted = productRepository.findById(productId).orElseThrow();
        assertThat(stillDeleted.getStock()).isEqualTo(0); // Stock remains 0 from logical delete
        assertThat(stillDeleted.isDeleted()).isTrue();
    }

    @Test
    void findAllByCreatorId_whenProductsExist_returnsPagedProducts() {
        // Given
        Long creatorId = 1L;
        createAndSaveProduct("Product1", 10.0, 5, creatorId);
        createAndSaveProduct("Product2", 20.0, 3, creatorId);
        createAndSaveProduct("Product3", 30.0, 8, 2L); // Different creator

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> page = productRepository.findAllByCreatorId(creatorId, pageable);

        // Then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Product1", "Product2");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findAllByCreatorId_whenNoProductsExist_returnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> page = productRepository.findAllByCreatorId(999L, pageable);

        // Then
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void findAllByCreatorId_withPagination_returnsCorrectPage() {
        // Given
        Long creatorId = 1L;
        for (int i = 1; i <= 15; i++) {
            createAndSaveProduct("Product" + i, 10.0 * i, i, creatorId);
        }

        Pageable pageable = PageRequest.of(1, 5); // Second page, 5 items per page

        // When
        Page<Product> page = productRepository.findAllByCreatorId(creatorId, pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getNumber()).isEqualTo(1); // Page number
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(15);
    }

    @Test
    void deleteByIdLogical_whenProductExists_marksAsDeleted() {
        // Given
        Product product = createAndSaveProduct("Product", 100.0, 10, 1L);
        String productId = product.getId();
        assertThat(product.isDeleted()).isFalse();

        // When
        productRepository.deleteByIdLogical(productId);
        productRepository.flush();
        entityManager.clear();

        // Then
        Product deleted = productRepository.findById(productId).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getStock()).isEqualTo(0);
    }

    @Test
    void deleteByIdLogical_doesNotPhysicallyDeleteProduct() {
        // Given
        Product product = createAndSaveProduct("Product", 100.0, 10, 1L);
        String productId = product.getId();

        // When
        productRepository.deleteByIdLogical(productId);
        productRepository.flush();
        entityManager.clear();

        // Then - Product still exists in database
        Optional<Product> stillExists = productRepository.findById(productId);
        assertThat(stillExists).isPresent();
        assertThat(stillExists.get().isDeleted()).isTrue();
    }

    @Test
    void findAll_withSpecification_filtersCorrectly() {
        // Given
        createAndSaveProduct("Laptop", 999.99, 5, 1L);
        createAndSaveProduct("Mouse", 29.99, 50, 1L);
        createAndSaveProduct("Keyboard", 79.99, 20, 1L);

        // Create specification: price >= 70
        Specification<Product> spec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), 70.0);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> page = productRepository.findAll(spec, pageable);

        // Then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Keyboard");
    }

    @Test
    void findAll_withMultipleSpecifications_filtersCorrectly() {
        // Given
        createAndSaveProduct("Product1", 50.0, 100, 1L);
        createAndSaveProduct("Product2", 150.0, 5, 1L);
        createAndSaveProduct("Product3", 200.0, 50, 1L);

        // Specification: price > 100 AND stock >= 10
        Specification<Product> priceSpec = (root, query, cb) -> cb.greaterThan(root.get("price"), 100.0);
        Specification<Product> stockSpec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("stock"), 10);
        Specification<Product> spec = priceSpec.and(stockSpec);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> page = productRepository.findAll(spec, pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Product3");
    }

    @Test
    void count_whenProductsExist_returnsCorrectCount() {
        // Given
        createAndSaveProduct("Product1", 10.0, 5, 1L);
        createAndSaveProduct("Product2", 20.0, 3, 1L);

        // When
        long count = productRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void update_whenProductExists_updatesSuccessfully() {
        // Given
        Product product = createAndSaveProduct("OldName", 100.0, 10, 1L);
        String productId = product.getId();

        // When
        product.setName("NewName");
        product.setPrice(200.0);
        productRepository.save(product);

        // Then
        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("NewName");
        assertThat(updated.getPrice()).isEqualTo(200.0);
    }

    /**
     * Helper method to create a product builder with common fields.
     */
    private Product.ProductBuilder createProductBuilder(String name, double price, int stock, Long creatorId) {
        return Product.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .description("Description for " + name)
                .creatorId(creatorId)
                .category(testCategory)
                .images(new ArrayList<>())
                .comments(new ArrayList<>());
    }

    /**
     * Helper method to create and save a product.
     */
    private Product createAndSaveProduct(String name, double price, int stock, Long creatorId) {
        Product product = createProductBuilder(name, price, stock, creatorId).build();
        return productRepository.save(product);
    }
}
