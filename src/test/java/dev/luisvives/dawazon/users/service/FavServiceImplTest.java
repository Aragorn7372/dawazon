package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.users.exceptions.UserException;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FavServiceImpl favService;

    private User testUser;
    private Product testProduct;
    private String productId;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        productId = "product-123";

        testUser = User.builder()
                .id(userId)
                .userName("testUser")
                .email("test@example.com")
                .favs(new ArrayList<>())
                .build();

        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(99.99)
                .build();
    }

    @Test
    void addFav_addProductToFavorites_whenUserExistsAndProductNotInFavorites() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        favService.addFav(productId, userId);

        assertTrue(testUser.getFavs().contains(productId));
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void addFav_throwUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserException.UserNotFoundException.class,
                () -> favService.addFav(productId, userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void addFav_throwUserHasThatFavProductException_whenProductAlreadyInFavorites() {
        testUser.getFavs().add(productId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertThrows(UserException.UserHasThatFavProductException.class,
                () -> favService.addFav(productId, userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeFav_removeProductFromFavorites_whenUserExistsAndProductInFavorites() {
        testUser.getFavs().add(productId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        favService.removeFav(productId, userId);

        assertFalse(testUser.getFavs().contains(productId));
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void removeFav_throwUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserException.UserNotFoundException.class,
                () -> favService.removeFav(productId, userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeFav_throwUserHasThatFavProductException_whenProductNotInFavorites() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertThrows(UserException.UserHasThatFavProductException.class,
                () -> favService.removeFav(productId, userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getFavs_returnPageOfProducts_whenUserHasFavoritesAndAllProductsExist() {
        String product1Id = "product-1";
        String product2Id = "product-2";
        testUser.getFavs().add(product1Id);
        testUser.getFavs().add(product2Id);

        Product product1 = Product.builder().id(product1Id).name("Product 1").build();
        Product product2 = Product.builder().id(product2Id).name("Product 2").build();

        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(product1Id)).thenReturn(Optional.of(product1));
        when(productRepository.findById(product2Id)).thenReturn(Optional.of(product2));

        Page<Product> result = favService.getFavs(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(product1));
        assertTrue(result.getContent().contains(product2));
        verify(userRepository).findById(userId);
        verify(productRepository).findById(product1Id);
        verify(productRepository).findById(product2Id);
    }

    @Test
    void getFavs_returnEmptyPage_whenUserHasNoFavorites() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        Page<Product> result = favService.getFavs(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(userRepository).findById(userId);
        verify(productRepository, never()).findById(anyString());
    }

    @Test
    void getFavs_filterOutNonExistentProducts_whenSomeProductsDoNotExist() {
        String product1Id = "product-1";
        String product2Id = "product-nonexistent";
        String product3Id = "product-3";
        testUser.getFavs().add(product1Id);
        testUser.getFavs().add(product2Id);
        testUser.getFavs().add(product3Id);

        Product product1 = Product.builder().id(product1Id).name("Product 1").build();
        Product product3 = Product.builder().id(product3Id).name("Product 3").build();

        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(product1Id)).thenReturn(Optional.of(product1));
        when(productRepository.findById(product2Id)).thenReturn(Optional.empty());
        when(productRepository.findById(product3Id)).thenReturn(Optional.of(product3));

        Page<Product> result = favService.getFavs(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(product1));
        assertTrue(result.getContent().contains(product3));
        assertFalse(result.getContent().stream().anyMatch(p -> p.getId().equals(product2Id)));
        verify(userRepository).findById(userId);
        verify(productRepository).findById(product1Id);
        verify(productRepository).findById(product2Id);
        verify(productRepository).findById(product3Id);
    }

    @Test
    void getFavs_throwUserNotFoundException_whenUserDoesNotExist() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserException.UserNotFoundException.class,
                () -> favService.getFavs(userId, pageable));

        verify(userRepository).findById(userId);
        verify(productRepository, never()).findById(anyString());
    }

    @Test
    void addFav_maintainMultipleFavorites_whenAddingSecondProduct() {
        String existingProductId = "product-existing";
        testUser.getFavs().add(existingProductId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        favService.addFav(productId, userId);

        assertEquals(2, testUser.getFavs().size());
        assertTrue(testUser.getFavs().contains(existingProductId));
        assertTrue(testUser.getFavs().contains(productId));
        verify(userRepository).save(testUser);
    }

    @Test
    void removeFav_maintainOtherFavorites_whenRemovingOneProduct() {
        String otherProductId = "product-other";
        testUser.getFavs().add(productId);
        testUser.getFavs().add(otherProductId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        favService.removeFav(productId, userId);

        assertEquals(1, testUser.getFavs().size());
        assertTrue(testUser.getFavs().contains(otherProductId));
        assertFalse(testUser.getFavs().contains(productId));
        verify(userRepository).save(testUser);
    }
}
