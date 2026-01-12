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
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void savewhenValidProductsavesSuccessfully() {
        Product product = createProductBuilder("Laptop", 999.99, 10, 1L).build();

        Product saved = productRepository.save(product);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Laptop");
        assertThat(saved.getPrice()).isEqualTo(999.99);
        assertThat(saved.getStock()).isEqualTo(10);
    }

    @Test
    void findByIdwhenProductExistsreturnsProduct() {
        Product product = createAndSaveProduct("Mouse", 29.99, 50, 1L);

        Optional<Product> found = productRepository.findById(product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Mouse");
    }

    @Test
    void findByIdwhenProductDoesNotExistreturnsEmpty() {
        Optional<Product> found = productRepository.findById("NON_EXISTENT");

        assertThat(found).isEmpty();
    }

    @Test
    void findAllBycreatedAtBetweenwhenProductsInRangereturnsProducts() {
        LocalDateTime before = LocalDateTime.now().minusHours(1);
        createAndSaveProduct("Product1", 10.0, 5, 1L);
        createAndSaveProduct("Product2", 20.0, 3, 1L);
        LocalDateTime after = LocalDateTime.now().plusHours(1);

        List<Product> products = productRepository.findAllBycreatedAtBetween(before, after);

        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Product1", "Product2");
    }

    @Test
    void findAllBycreatedAtBetweenwhenNoProductsInRangereturnsEmpty() {
        createAndSaveProduct("OldProduct", 10.0, 5, 1L);
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(2);

        List<Product> products = productRepository.findAllBycreatedAtBetween(futureStart, futureEnd);

        assertThat(products).isEmpty();
    }

    @Test
    void substractStockwhenSufficientStockupdatesSuccessfully() {
        Product product = createAndSaveProduct("Product", 100.0, 10, 1L);
        String productId = product.getId();
        Long version = product.getVersion();

        int rowsAffected = productRepository.substractStock(productId, 5, version);
        productRepository.flush();
        entityManager.clear();
        assertThat(rowsAffected).isEqualTo(1); // 1 row affected
        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(5);
    }

    @Test
    void substractStockwhenInsufficientStockdoesNotUpdate() {
        Product product = createAndSaveProduct("Product", 100.0, 3, 1L);
        String productId = product.getId();
        Long version = product.getVersion();

        int rowsAffected = productRepository.substractStock(productId, 5, version);
        productRepository.flush();

        assertThat(rowsAffected).isEqualTo(0);
        Product unchanged = productRepository.findById(productId).orElseThrow();
        assertThat(unchanged.getStock()).isEqualTo(3);
    }

    @Test
    void substractStockwhenProductDeleteddoesNotUpdate() {
        Product product = createAndSaveProduct("Product", 100.0, 10, 1L);
        String productId = product.getId();

        productRepository.deleteByIdLogical(productId);
        productRepository.flush();
        entityManager.clear();

        Product deletedProduct = productRepository.findById(productId).orElseThrow();
        Long newVersion = deletedProduct.getVersion();

        int rowsAffected = productRepository.substractStock(productId, 5, newVersion);
        productRepository.flush();
        // Then - No rows affected because product is deleted
        assertThat(rowsAffected).isEqualTo(0);
        entityManager.clear();
        Product stillDeleted = productRepository.findById(productId).orElseThrow();
        assertThat(stillDeleted.getStock()).isEqualTo(0);
        assertThat(stillDeleted.isDeleted()).isTrue();
    }

    @Test
    void findAllByCreatorIdwhenProductsExistreturnsPagedProducts() {
        Long creatorId = 1L;
        createAndSaveProduct("Product1", 10.0, 5, creatorId);
        createAndSaveProduct("Product2", 20.0, 3, creatorId);
        createAndSaveProduct("Product3", 30.0, 8, 2L); // Different creator

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> page = productRepository.findAllByCreatorId(creatorId, pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Product1", "Product2");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findAllByCreatorIdwhenNoProductsExistreturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> page = productRepository.findAllByCreatorId(999L, pageable);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void findAllByCreatorIdwithPaginationreturnsCorrectPage() {
        Long creatorId = 1L;
        for (int i = 1; i <= 15; i++) {
            createAndSaveProduct("Product" + i, 10.0 * i, i, creatorId);
        }

        Pageable pageable = PageRequest.of(1, 5);

        Page<Product> page = productRepository.findAllByCreatorId(creatorId, pageable);

        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(15);
    }

    @Test
    void deleteByIdLogicalwhenProductExistsmarksAsDeleted() {
        Product product = createAndSaveProduct("Product", 100.0, 10, 1L);
        String productId = product.getId();
        assertThat(product.isDeleted()).isFalse();

        productRepository.deleteByIdLogical(productId);
        productRepository.flush();
        entityManager.clear();

        Product deleted = productRepository.findById(productId).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getStock()).isEqualTo(0);
    }

    @Test
    void deleteByIdLogicaldoesNotPhysicallyDeleteProduct() {
        Product product = createAndSaveProduct("Product", 100.0, 10, 1L);
        String productId = product.getId();

        productRepository.deleteByIdLogical(productId);
        productRepository.flush();
        entityManager.clear();

        Optional<Product> stillExists = productRepository.findById(productId);
        assertThat(stillExists).isPresent();
        assertThat(stillExists.get().isDeleted()).isTrue();
    }

    @Test
    void findAllwithSpecificationfiltersCorrectly() {
        createAndSaveProduct("Laptop", 999.99, 5, 1L);
        createAndSaveProduct("Mouse", 29.99, 50, 1L);
        createAndSaveProduct("Keyboard", 79.99, 20, 1L);

        Specification<Product> spec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), 70.0);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> page = productRepository.findAll(spec, pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Keyboard");
    }

    @Test
    void findAllwithMultipleSpecificationsfiltersCorrectly() {
        createAndSaveProduct("Product1", 50.0, 100, 1L);
        createAndSaveProduct("Product2", 150.0, 5, 1L);
        createAndSaveProduct("Product3", 200.0, 50, 1L);

        Specification<Product> priceSpec = (root, query, cb) -> cb.greaterThan(root.get("price"), 100.0);
        Specification<Product> stockSpec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("stock"), 10);
        Specification<Product> spec = priceSpec.and(stockSpec);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> page = productRepository.findAll(spec, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Product3");
    }

    @Test
    void countwhenProductsExistreturnsCorrectCount() {
        createAndSaveProduct("Product1", 10.0, 5, 1L);
        createAndSaveProduct("Product2", 20.0, 3, 1L);

        long count = productRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void updatewhenProductExistsupdatesSuccessfully() {
        Product product = createAndSaveProduct("OldName", 100.0, 10, 1L);
        String productId = product.getId();

        product.setName("NewName");
        product.setPrice(200.0);
        productRepository.save(product);

        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("NewName");
        assertThat(updated.getPrice()).isEqualTo(200.0);
    }


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


    private Product createAndSaveProduct(String name, double price, int stock, Long creatorId) {
        Product product = createProductBuilder(name, price, stock, creatorId).build();
        return productRepository.save(product);
    }
}
