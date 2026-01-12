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


class CategoryRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void savewhenValidCategorysaveSuccessfully() {
        Category category = new Category();
        category.setName("Electronics");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Electronics");
    }

    @Test
    void findByIdwhenCategoryExistsreturnsCategory() {
        Category category = createAndSaveCategory("Books");

        Optional<Category> found = categoryRepository.findById(category.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Books");
    }

    @Test
    void findByIdwhenCategoryDoesNotExistreturnsEmpty() {
        Optional<Category> found = categoryRepository.findById("NON_EXISTENT");

        assertThat(found).isEmpty();
    }

    @Test
    void findByNameIgnoreCasewhenCategoryExistsreturnsCategory() {
        createAndSaveCategory("Electronics");

        Optional<Category> found = categoryRepository.findByNameIgnoreCase("electronics");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void findByNameIgnoreCasewhenSearchingWithDifferentCasefindsCategory() {
        createAndSaveCategory("Sports");

        Optional<Category> foundLower = categoryRepository.findByNameIgnoreCase("sports");
        Optional<Category> foundUpper = categoryRepository.findByNameIgnoreCase("SPORTS");
        Optional<Category> foundMixed = categoryRepository.findByNameIgnoreCase("SpOrTs");

        assertThat(foundLower).isPresent();
        assertThat(foundUpper).isPresent();
        assertThat(foundMixed).isPresent();
        assertThat(foundLower.get().getName()).isEqualTo("Sports");
        assertThat(foundUpper.get().getName()).isEqualTo("Sports");
        assertThat(foundMixed.get().getName()).isEqualTo("Sports");
    }

    @Test
    void findByNameIgnoreCasewhenCategoryDoesNotExistreturnsEmpty() {
        Optional<Category> found = categoryRepository.findByNameIgnoreCase("NonExistent");

        assertThat(found).isEmpty();
    }

    @Test
    void getAllNameswhenMultipleCategoriesExistreturnsAllNames() {
        createAndSaveCategory("Electronics");
        createAndSaveCategory("Books");
        createAndSaveCategory("Sports");

        List<String> names = categoryRepository.getAllNames();

        assertThat(names).hasSize(3);
        assertThat(names).containsExactlyInAnyOrder("Electronics", "Books", "Sports");
    }

    @Test
    void getAllNameswhenNoCategoriesExistreturnsEmptyList() {
        List<String> names = categoryRepository.getAllNames();

        assertThat(names).isEmpty();
    }

    @Test
    void findAllwhenMultipleCategoriesExistreturnsAllCategories() {
        createAndSaveCategory("Category1");
        createAndSaveCategory("Category2");
        createAndSaveCategory("Category3");

        List<Category> categories = categoryRepository.findAll();

        assertThat(categories).hasSize(3);
        assertThat(categories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Category1", "Category2", "Category3");
    }

    @Test
    void findAllwhenNoCategoriesExistreturnsEmptyList() {
        List<Category> categories = categoryRepository.findAll();

        assertThat(categories).isEmpty();
    }

    @Test
    void deleteByIdwhenCategoryExistsdeletesSuccessfully() {
        Category category = createAndSaveCategory("ToDelete");
        String categoryId = category.getId();
        assertThat(categoryRepository.findById(categoryId)).isPresent();

        categoryRepository.deleteById(categoryId);

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    void deleteAllwhenMultipleCategoriesExistdeletesAllCategories() {
        createAndSaveCategory("Category1");
        createAndSaveCategory("Category2");
        createAndSaveCategory("Category3");
        assertThat(categoryRepository.findAll()).hasSize(3);

        categoryRepository.deleteAll();

        assertThat(categoryRepository.findAll()).isEmpty();
    }

    @Test
    void countwhenCategoriesExistreturnsCorrectCount() {
        createAndSaveCategory("Category1");
        createAndSaveCategory("Category2");

        long count = categoryRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void updatewhenCategoryExistsupdatesSuccessfully() {
        Category category = createAndSaveCategory("OldName");
        String categoryId = category.getId();

        category.setName("NewName");
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        Optional<Category> updated = categoryRepository.findById(categoryId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("NewName");
    }


    private Category createAndSaveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }
}
