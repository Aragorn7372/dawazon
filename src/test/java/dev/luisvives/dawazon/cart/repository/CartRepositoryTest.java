package dev.luisvives.dawazon.cart.repository;

import dev.luisvives.dawazon.BaseMongoRepositoryTest;
import dev.luisvives.dawazon.cart.models.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CartRepositoryTest extends BaseMongoRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    void cleanDatabase() {
        cartRepository.deleteAll();
    }

    @AfterEach
    void cleanup() {
        cartRepository.deleteAll();
    }

    @Test
    void findByUserId_whenUserHasMultipleCarts_returnsPagedCarts() {
        Long userId = 1L;
        Cart firstCart = createSampleCart(userId, false);
        Cart secondCart = createSampleCart(userId, true);
        cartRepository.saveAll(List.of(firstCart, secondCart));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Cart> result = cartRepository.findByUserId(userId, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByUserId_whenUserHasNoCarts_returnsEmptyPage() {
        Long userId = 99L;
        Pageable pageable = PageRequest.of(0, 10);

        Page<Cart> result = cartRepository.findByUserId(userId, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findByUserId_withPagination_returnsCorrectPageSize() {
        Long userId = 2L;
        for (int i = 0; i < 5; i++) {
            cartRepository.save(createSampleCart(userId, false));
        }

        Pageable pageable = PageRequest.of(0, 3);
        Page<Cart> result = cartRepository.findByUserId(userId, pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findByUserIdAndPurchased_whenActiveCartExists_returnsActiveCart() {
        Long userId = 3L;
        Cart activeCart = createSampleCart(userId, false);
        cartRepository.save(activeCart);

        Optional<Cart> result = cartRepository.findByUserIdAndPurchased(userId, false);

        assertThat(result).isPresent();
        assertThat(result.get().isPurchased()).isFalse();
        assertThat(result.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void findByUserIdAndPurchased_whenPurchasedCartExists_returnsPurchasedCart() {
        Long userId = 4L;
        Cart purchasedCart = createSampleCart(userId, true);
        Cart activeCart = createSampleCart(userId, false);
        cartRepository.saveAll(List.of(purchasedCart, activeCart));

        Optional<Cart> result = cartRepository.findByUserIdAndPurchased(userId, true);

        assertThat(result).isPresent();
        assertThat(result.get().isPurchased()).isTrue();
        assertThat(result.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void findByUserIdAndPurchased_whenNoMatchingCart_returnsEmpty() {
        Long userId = 5L;
        Cart purchasedCart = createSampleCart(userId, true);
        cartRepository.save(purchasedCart);

        Optional<Cart> result = cartRepository.findByUserIdAndPurchased(userId, false);

        assertThat(result).isEmpty();
    }

    @Test
    void addCartLine_whenCartExists_addsNewLineSuccessfully() {
        Long userId = 6L;
        ObjectId cartId = new ObjectId();
        Cart cart = createSampleCartWithId(cartId, userId, false);
        cart.setCartLines(new ArrayList<>());
        Cart savedCart = cartRepository.save(cart);
        int initialSize = savedCart.getCartLines().size();

        CartLine newLine = createCartLine("PROD-NEW", 5, 100.0, Status.EN_CARRITO);
        long modifiedCount = cartRepository.addCartLine(cartId, newLine);

        assertThat(modifiedCount).isEqualTo(1);

        Cart updatedCart = cartRepository.findById(cartId).orElseThrow();
        assertThat(updatedCart.getCartLines()).hasSize(initialSize + 1);
        assertThat(updatedCart.getCartLines())
                .anyMatch(line -> line.getProductId().equals("PROD-NEW") && line.getQuantity() == 5);
    }

    @Test
    void addCartLine_toEmptyCart_addsFirstLine() {
        Long userId = 7L;
        ObjectId cartId = new ObjectId();
        Cart cart = createCartWithEmptyLinesWithId(cartId, userId);
        cartRepository.save(cart);

        CartLine firstLine = createCartLine("PROD-001", 1, 50.0, Status.EN_CARRITO);
        long modifiedCount = cartRepository.addCartLine(cartId, firstLine);

        assertThat(modifiedCount).isEqualTo(1);

        Cart updatedCart = cartRepository.findById(cartId).orElseThrow();
        assertThat(updatedCart.getCartLines()).hasSize(1);
        assertThat(updatedCart.getCartLines().get(0).getProductId()).isEqualTo("PROD-001");
    }

    @Test
    void addCartLine_whenCartNotFound_returnsZeroModified() {
        ObjectId nonExistentId = new ObjectId();
        CartLine newLine = createCartLine("PROD-999", 1, 10.0, Status.EN_CARRITO);

        long modifiedCount = cartRepository.addCartLine(nonExistentId, newLine);

        assertThat(modifiedCount).isZero();
    }

    @Test
    void removeCartLine_whenLineExists_removesLineSuccessfully() {
        Long userId = 8L;
        ObjectId cartId = new ObjectId();
        CartLine lineToRemove = createCartLine("PROD-REMOVE", 2, 75.0, Status.EN_CARRITO);
        Cart cart = createSampleCartWithId(cartId, userId, false);
        cart.setCartLines(new ArrayList<>(List.of(lineToRemove)));
        cartRepository.save(cart);

        long modifiedCount = cartRepository.removeCartLine(cartId, lineToRemove);

        assertThat(modifiedCount).isEqualTo(1);

        Cart updatedCart = cartRepository.findById(cartId).orElseThrow();
        assertThat(updatedCart.getCartLines()).isEmpty();
    }

    @Test
    void removeCartLine_whenLineNotFound_returnsZeroModified() {
        Long userId = 9L;
        ObjectId cartId = new ObjectId();
        Cart cart = createSampleCartWithId(cartId, userId, false);
        cartRepository.save(cart);

        CartLine nonExistentLine = createCartLine("PROD-NONEXISTENT", 1, 10.0, Status.EN_CARRITO);
        long modifiedCount = cartRepository.removeCartLine(cartId, nonExistentLine);

        assertThat(modifiedCount).isZero();
    }

    @Test
    void removeCartLine_fromCartWithMultipleLines_removesOnlySpecifiedLine() {
        Long userId = 10L;
        ObjectId cartId = new ObjectId();
        CartLine lineToKeep = createCartLine("PROD-KEEP", 3, 30.0, Status.EN_CARRITO);
        CartLine lineToRemove = createCartLine("PROD-REMOVE", 1, 20.0, Status.EN_CARRITO);
        Cart cart = createSampleCartWithId(cartId, userId, false);
        cart.setCartLines(new ArrayList<>(List.of(lineToKeep, lineToRemove)));
        cartRepository.save(cart);

        long modifiedCount = cartRepository.removeCartLine(cartId, lineToRemove);

        assertThat(modifiedCount).isEqualTo(1);

        Cart updatedCart = cartRepository.findById(cartId).orElseThrow();
        assertThat(updatedCart.getCartLines()).hasSize(1);
        assertThat(updatedCart.getCartLines().get(0).getProductId()).isEqualTo("PROD-KEEP");
    }

    @Test
    void updateCartLineStatus_whenLineExists_updatesStatusSuccessfully() {
        Long userId = 11L;
        ObjectId cartId = new ObjectId();
        String productId = "PROD-UPDATE";
        CartLine line = createCartLine(productId, 2, 40.0, Status.EN_CARRITO);
        Cart cart = createSampleCartWithId(cartId, userId, false);
        cart.setCartLines(new ArrayList<>(List.of(line)));
        cartRepository.save(cart);

        long modifiedCount = cartRepository.updateCartLineStatus(cartId, productId, Status.ENVIADO);

        assertThat(modifiedCount).isEqualTo(1);

        Cart updatedCart = cartRepository.findById(cartId).orElseThrow();
        CartLine updatedLine = updatedCart.getCartLines().stream()
                .filter(l -> l.getProductId().equals(productId))
                .findFirst()
                .orElseThrow();
        assertThat(updatedLine.getStatus()).isEqualTo(Status.ENVIADO);
    }

    @Test
    void updateCartLineStatus_whenProductNotInCart_returnsZeroModified() {
        Long userId = 12L;
        ObjectId cartId = new ObjectId();
        Cart cart = createSampleCartWithId(cartId, userId, false);
        cartRepository.save(cart);

        long modifiedCount = cartRepository.updateCartLineStatus(
                cartId,
                "PROD-NONEXISTENT",
                Status.CANCELADO);

        assertThat(modifiedCount).isZero();
    }

    @Test
    void updateCartLineStatus_updatesCorrectLineInMultiLineCart() {
        Long userId = 13L;
        ObjectId cartId = new ObjectId();
        String targetProductId = "PROD-TARGET";
        CartLine targetLine = createCartLine(targetProductId, 1, 50.0, Status.EN_CARRITO);
        CartLine otherLine = createCartLine("PROD-OTHER", 2, 30.0, Status.EN_CARRITO);
        Cart cart = createSampleCartWithId(cartId, userId, false);
        cart.setCartLines(new ArrayList<>(List.of(targetLine, otherLine)));
        cartRepository.save(cart);

        long modifiedCount = cartRepository.updateCartLineStatus(
                cartId,
                targetProductId,
                Status.PREPARADO);

        assertThat(modifiedCount).isEqualTo(1);

        Cart updatedCart = cartRepository.findById(cartId).orElseThrow();
        assertThat(updatedCart.getCartLines()).hasSize(2);

        CartLine updatedTargetLine = updatedCart.getCartLines().stream()
                .filter(l -> l.getProductId().equals(targetProductId))
                .findFirst()
                .orElseThrow();
        assertThat(updatedTargetLine.getStatus()).isEqualTo(Status.PREPARADO);

        CartLine unchangedLine = updatedCart.getCartLines().stream()
                .filter(l -> l.getProductId().equals("PROD-OTHER"))
                .findFirst()
                .orElseThrow();
        assertThat(unchangedLine.getStatus()).isEqualTo(Status.EN_CARRITO);
    }

    private Cart createSampleCart(Long userId, boolean purchased) {
        return Cart.builder()
                .id(new ObjectId())
                .userId(userId)
                .purchased(purchased)
                .client(createSampleClient())
                .cartLines(List.of(createCartLine("PROD-123", 2, 50.0, Status.EN_CARRITO)))
                .totalItems(1)
                .total(100.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .checkoutInProgress(false)
                .build();
    }

    private Cart createSampleCartWithId(ObjectId id, Long userId, boolean purchased) {
        return Cart.builder()
                .id(id)
                .userId(userId)
                .purchased(purchased)
                .client(createSampleClient())
                .cartLines(List.of(createCartLine("PROD-123", 2, 50.0, Status.EN_CARRITO)))
                .totalItems(1)
                .total(100.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .checkoutInProgress(false)
                .build();
    }

    private Cart createCartWithEmptyLinesWithId(ObjectId id, Long userId) {
        return Cart.builder()
                .id(id)
                .userId(userId)
                .purchased(false)
                .client(createSampleClient())
                .cartLines(new ArrayList<>())
                .totalItems(0)
                .total(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .checkoutInProgress(false)
                .build();
    }

    private Client createSampleClient() {
        return Client.builder()
                .name("Test User")
                .email("test@example.com")
                .phone("123456789")
                .address(createSampleAddress())
                .build();
    }

    private Address createSampleAddress() {
        return Address.builder()
                .number((short) 123)
                .street("Test Street")
                .city("Test City")
                .province("Test Province")
                .country("Spain")
                .postalCode(28001)
                .build();
    }

    private CartLine createCartLine(String productId, Integer quantity, Double price, Status status) {
        return CartLine.builder()
                .productId(productId)
                .quantity(quantity)
                .productPrice(price)
                .status(status)
                .totalPrice(quantity * price)
                .build();
    }
}
