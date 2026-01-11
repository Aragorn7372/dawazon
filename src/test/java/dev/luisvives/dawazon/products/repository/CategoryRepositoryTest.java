package dev.luisvives.dawazon.products.repository;

import dev.luisvives.dawazon.BaseRepositoryTest;
import dev.luisvives.dawazon.products.models.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for CategoryRepository using TestContainers.
 * <p>
 * Tests following FIRST principles:
 * - Fast: TestContainers reuses containers between tests
 * - Independent: Each test cleans up its data
 * - Repeatable: Uses isolated PostgreSQL container
 * - Self-validating: Clear assertions for all operations
 * - Timely: Tests repository functionality with real database
 * </p>
 */
class CategoryRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        categoryRepository.deleteAll();
    }

    @Test
    void save_whenValidCategory_saveSuccessfully() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // When
        Category savedCategory = categoryRepository.save(category);

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Electronics");
    }

    @Test
    void findById_whenCategoryExists_returnsCategory() {
        // Given
        Category category = createAndSaveCategory("Books");

        // When
        Optional<Category> found = categoryRepository.findById(category.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Books");
    }

    @Test
    void findById_whenCategoryDoesNotExist_returnsEmpty() {
        // When
        Optional<Category> found = categoryRepository.findById("NON_EXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByNameIgnoreCase_whenCategoryExists_returnsCategory() {
        // Given
        createAndSaveCategory("Electronics");

        // When
        Optional<Category> found = categoryRepository.findByNameIgnoreCase("electronics");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void findByNameIgnoreCase_whenSearchingWithDifferentCase_findsCategory() {
        // Given
        createAndSaveCategory("Sports");

        // When
        Optional<Category> foundLower = categoryRepository.findByNameIgnoreCase("sports");
        Optional<Category> foundUpper = categoryRepository.findByNameIgnoreCase("SPORTS");
        Optional<Category> foundMixed = categoryRepository.findByNameIgnoreCase("SpOrTs");

        // Then
        assertThat(foundLower).isPresent();
        assertThat(foundUpper).isPresent();
        assertThat(foundMixed).isPresent();
        assertThat(foundLower.get().getName()).isEqualTo("Sports");
        assertThat(foundUpper.get().getName()).isEqualTo("Sports");
        assertThat(foundMixed.get().getName()).isEqualTo("Sports");
    }

    @Test
    void findByNameIgnoreCase_whenCategoryDoesNotExist_returnsEmpty() {
        // When
        Optional<Category> found = categoryRepository.findByNameIgnoreCase("NonExistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void getAllNames_whenMultipleCategoriesExist_returnsAllNames() {
        // Given
        createAndSaveCategory("Electronics");
        createAndSaveCategory("Books");
        createAndSaveCategory("Sports");

        // When
        List<String> names = categoryRepository.getAllNames();

        // Then
        assertThat(names).hasSize(3);
        assertThat(names).containsExactlyInAnyOrder("Electronics", "Books", "Sports");
    }

    @Test
    void getAllNames_whenNoCategoriesExist_returnsEmptyList() {
        // When
        List<String> names = categoryRepository.getAllNames();

        // Then
        assertThat(names).isEmpty();
    }

    @Test
    void findAll_whenMultipleCategoriesExist_returnsAllCategories() {
        // Given
        createAndSaveCategory("Category1");
        createAndSaveCategory("Category2");
        createAndSaveCategory("Category3");

        // When
        List<Category> categories = categoryRepository.findAll();

        // Then
        assertThat(categories).hasSize(3);
        assertThat(categories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Category1", "Category2", "Category3");
    }

    @Test
    void findAll_whenNoCategoriesExist_returnsEmptyList() {
        // When
        List<Category> categories = categoryRepository.findAll();

        // Then
        assertThat(categories).isEmpty();
    }

    @Test
    void deleteById_whenCategoryExists_deletesSuccessfully() {
        // Given
        Category category = createAndSaveCategory("ToDelete");
        String categoryId = category.getId();
        assertThat(categoryRepository.findById(categoryId)).isPresent();

        // When
        categoryRepository.deleteById(categoryId);

        // Then
        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    void deleteAll_whenMultipleCategoriesExist_deletesAllCategories() {
        // Given
        createAndSaveCategory("Category1");
        createAndSaveCategory("Category2");
        createAndSaveCategory("Category3");
        assertThat(categoryRepository.findAll()).hasSize(3);

        // When
        categoryRepository.deleteAll();

        // Then
        assertThat(categoryRepository.findAll()).isEmpty();
    }

    @Test
    void count_whenCategoriesExist_returnsCorrectCount() {
        // Given
        createAndSaveCategory("Category1");
        createAndSaveCategory("Category2");

        // When
        long count = categoryRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void update_whenCategoryExists_updatesSuccessfully() {
        // Given
        Category category = createAndSaveCategory("OldName");
        String categoryId = category.getId();

        // When
        category.setName("NewName");
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        // Then
        Optional<Category> updated = categoryRepository.findById(categoryId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("NewName");
    }

    /**
     * Helper method to create and save a category.
     */
    private Category createAndSaveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }
}
