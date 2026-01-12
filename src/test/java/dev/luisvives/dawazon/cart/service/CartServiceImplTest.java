package dev.luisvives.dawazon.cart.service;

import dev.luisvives.dawazon.cart.dto.CartStockRequestDto;
import dev.luisvives.dawazon.cart.exceptions.CartException;
import dev.luisvives.dawazon.cart.mapper.CartMapper;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.cart.models.Client;
import dev.luisvives.dawazon.cart.models.Status;
import dev.luisvives.dawazon.cart.repository.CartRepository;
import dev.luisvives.dawazon.common.email.OrderEmailService;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.stripe.service.StripeService;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private OrderEmailService orderEmailService;

    @InjectMocks
    private CartServiceImpl cartService;

    private ObjectId testCartId;
    private String testProductId;
    private Long testUserId;
    private Product testProduct;
    private Cart testCart;
    private User testUser;

    @BeforeEach
    void setUp() {
        testCartId = new ObjectId();
        testProductId = "PROD-123";
        testUserId = 1L;

        testProduct = Product.builder()
                .id(testProductId)
                .price(50.0)
                .stock(10)
                .creatorId(testUserId)
                .build();

        testUser = User.builder()
                .id(testUserId)
                .client(Client.builder().name("Test User").build())
                .build();

        testCart = Cart.builder()
                .id(testCartId)
                .userId(testUserId)
                .purchased(false)
                .client(testUser.getClient())
                .cartLines(new ArrayList<>())
                .totalItems(0)
                .total(0.0)
                .build();
    }

    @Test
    void addProductwhenProductAndCartExistaddsProductSuccessfully() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.addProduct(testCartId, testProductId);

        assertThat(result).isNotNull();
        verify(productRepository).findById(testProductId);
        verify(cartRepository).addCartLine(eq(testCartId), any(CartLine.class));
        verify(cartRepository).findById(testCartId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addProductwhenProductNotFoundthrowsProductNotFoundException() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProduct(testCartId, testProductId))
                .isInstanceOf(ProductException.NotFoundException.class);

        verify(productRepository).findById(testProductId);
        verify(cartRepository, never()).addCartLine(any(), any());
    }

    @Test
    void addProductwhenCartNotFoundthrowsCartNotFoundException() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProduct(testCartId, testProductId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(productRepository).findById(testProductId);
        verify(cartRepository).addCartLine(any(), any());
        verify(cartRepository).findById(testCartId);
    }

    @Test
    void removeProductwhenProductInCartremovesSuccessfully() {
        CartLine existingLine = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .status(Status.EN_CARRITO)
                .totalPrice(100.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(existingLine)));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.removeProduct(testCartId, testProductId);

        assertThat(result).isNotNull();
        verify(cartRepository, times(2)).findById(testCartId);
        verify(cartRepository).removeCartLine(eq(testCartId), any(CartLine.class));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void removeProductwhenCartNotFoundthrowsCartNotFoundException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeProduct(testCartId, testProductId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
        verify(cartRepository, never()).removeCartLine(any(), any());
    }

    @Test
    void getByIdwhenCartExistsreturnsCart() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        Cart result = cartService.getById(testCartId);

        assertThat(result).isEqualTo(testCart);
        verify(cartRepository).findById(testCartId);
    }

    @Test
    void getByIdwhenCartNotFoundthrowsCartNotFoundException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getById(testCartId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void saveWhenCartSavedmarksAsPurchasedAndCreatesNewCart() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(1)
                .status(Status.EN_CARRITO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.save(testCart);

        assertThat(result.isPurchased()).isTrue();
        assertThat(result.isCheckoutInProgress()).isFalse();
        assertThat(result.getCheckoutStartedAt()).isNull();
        verify(cartRepository, times(2)).save(any(Cart.class));
        verify(userRepository).findById(testUserId);
    }

    @Test
    void savewhenCartSavedsetsAllLinesToPreparado() {
        CartLine line1 = CartLine.builder()
                .productId("PROD-1")
                .quantity(1)
                .status(Status.EN_CARRITO)
                .build();

        CartLine line2 = CartLine.builder()
                .productId("PROD-2")
                .quantity(2)
                .status(Status.EN_CARRITO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line1, line2)));

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.save(testCart);

        assertThat(result.getCartLines()).allMatch(line -> line.getStatus() == Status.PREPARADO);
        verify(cartRepository, times(2)).save(any(Cart.class));
        verify(userRepository).findById(testUserId);
    }

    @Test
    void updateStockWithValidationwhenValidQuantityupdatesSuccessfully() {
        CartLine existingLine = CartLine.builder()
                .productId(testProductId)
                .quantity(1)
                .productPrice(50.0)
                .status(Status.EN_CARRITO)
                .totalPrice(50.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(existingLine)));

        CartStockRequestDto requestDto = new CartStockRequestDto();
        requestDto.setCartId(testCartId);
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(5);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.updateStockWithValidation(requestDto);

        assertThat(result).isNotNull();
        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateStockWithValidationwhenQuantityLessThanOnethrowsIllegalArgumentException() {
        CartStockRequestDto requestDto = new CartStockRequestDto();
        requestDto.setCartId(testCartId);
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(0);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.updateStockWithValidation(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La cantidad mínima es 1");

        verify(cartRepository).findById(testCartId);
        verify(productRepository, never()).findById(any());
    }

    @Test
    void updateStockWithValidationwhenInsufficientStockthrowsInsufficientStockException() {
        CartLine existingLine = CartLine.builder()
                .productId(testProductId)
                .quantity(1)
                .productPrice(50.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(existingLine)));

        CartStockRequestDto requestDto = new CartStockRequestDto();
        requestDto.setCartId(testCartId);
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(100);

        testProduct.setStock(5);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.updateStockWithValidation(requestDto))
                .isInstanceOf(CartException.InsufficientStockException.class);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateStockWithValidationwhenProductNotFoundthrowsProductNotFoundException() {
        CartStockRequestDto requestDto = new CartStockRequestDto();
        requestDto.setCartId(testCartId);
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(5);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateStockWithValidation(requestDto))
                .isInstanceOf(ProductException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
    }

    @Test
    void createNewCartwhenUserExistscreatesNewCart() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.createNewCart(testUserId);

        assertThat(result).isNotNull();
        verify(userRepository).findById(testUserId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void checkoutwhenStockAvailableprocessesCheckoutSuccessfully() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        String expectedPaymentUrl = "https://stripe.com/checkout/session";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(stripeService.createCheckoutSession(any(Cart.class))).thenReturn(expectedPaymentUrl);

        String paymentUrl = cartService.checkout(testCartId, testCart);

        assertThat(paymentUrl).isEqualTo(expectedPaymentUrl);
        assertThat(testCart.isCheckoutInProgress()).isTrue();
        assertThat(testCart.getCheckoutStartedAt()).isNotNull();
        verify(productRepository).findById(testProductId);
        verify(productRepository).save(any(Product.class));
        verify(stripeService).createCheckoutSession(testCart);
    }

    @Test
    void checkoutwhenInsufficientStockthrowsProductQuantityExceededException() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(20)
                .productPrice(50.0)
                .totalPrice(1000.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        testProduct.setStock(5);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.checkout(testCartId, testCart))
                .isInstanceOf(CartException.ProductQuantityExceededException.class);

        verify(productRepository).findById(testProductId);
        verify(productRepository, never()).save(any());
        verify(stripeService, never()).createCheckoutSession(any());
    }

    @Test
    void restoreStockwhenCartNotPurchasedrestoresStock() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(3)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));
        testCart.setPurchased(false);
        testCart.setCheckoutInProgress(true);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.restoreStock(testCartId);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
        verify(productRepository).save(any(Product.class));
        verify(cartRepository).save(testCart);
        assertThat(testCart.isCheckoutInProgress()).isFalse();
        assertThat(testCart.getCheckoutStartedAt()).isNull();
    }

    @Test
    void restoreStockwhenCartPurchaseddoesNotRestoreStock() {
        testCart.setPurchased(true);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        cartService.restoreStock(testCartId);

        verify(cartRepository).findById(testCartId);
        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void deleteByIdwhenCartExistsemptiesCart() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.deleteById(testCartId);

        assertThat(testCart.getCartLines()).isEmpty();
        verify(cartRepository).findById(testCartId);
        verify(cartRepository).save(testCart);
    }

    @Test
    void deleteByIdwhenCartNotFoundthrowsCartNotFoundException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteById(testCartId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void variosPorIdwhenAllProductsExistreturnsAllProducts() {
        String productId1 = "PROD-1";
        String productId2 = "PROD-2";
        List<String> productIds = List.of(productId1, productId2);

        Product product1 = Product.builder().id(productId1).build();
        Product product2 = Product.builder().id(productId2).build();

        when(productRepository.findById(productId1)).thenReturn(Optional.of(product1));
        when(productRepository.findById(productId2)).thenReturn(Optional.of(product2));

        List<Product> result = cartService.variosPorId(productIds);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(product1, product2);
        verify(productRepository).findById(productId1);
        verify(productRepository).findById(productId2);
    }

    @Test
    void variosPorIdwhenProductNotFoundthrowsProductNotFoundException() {
        String productId1 = "PROD-1";
        String productId2 = "PROD-MISSING";
        List<String> productIds = List.of(productId1, productId2);

        Product product1 = Product.builder().id(productId1).build();

        when(productRepository.findById(productId1)).thenReturn(Optional.of(product1));
        when(productRepository.findById(productId2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.variosPorId(productIds))
                .isInstanceOf(ProductException.NotFoundException.class);

        verify(productRepository).findById(productId1);
        verify(productRepository).findById(productId2);
    }

    @Test
    void getCartByUserIdwhenUserHasActiveCartreturnsCart() {
        when(cartRepository.findByUserIdAndPurchased(testUserId, false))
                .thenReturn(Optional.of(testCart));

        Cart result = cartService.getCartByUserId(testUserId);

        assertThat(result).isEqualTo(testCart);
        verify(cartRepository).findByUserIdAndPurchased(testUserId, false);
    }

    @Test
    void getCartByUserIdwhenNoActiveCartthrowsCartNotFoundException() {
        when(cartRepository.findByUserIdAndPurchased(testUserId, false))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCartByUserId(testUserId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findByUserIdAndPurchased(testUserId, false);
    }

    @Test
    void cancelSalewhenAdminCancelcancelsSuccessfully() {
        String cartIdStr = testCartId.toHexString();
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(3)
                .status(Status.PREPARADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.cancelSale(cartIdStr, testProductId, testUserId, true);

        assertThat(line.getStatus()).isEqualTo(Status.CANCELADO);
        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
        verify(productRepository).save(testProduct);
        verify(cartRepository).save(testCart);
    }

    @Test
    void cancelSalewhenManagerOwnsProductcancelsSuccessfully() {
        String cartIdStr = testCartId.toHexString();
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.PREPARADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));
        testProduct.setCreatorId(testUserId);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.cancelSale(cartIdStr, testProductId, testUserId, false);

        assertThat(line.getStatus()).isEqualTo(Status.CANCELADO);
        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
        verify(productRepository).save(testProduct);
        verify(cartRepository).save(testCart);
    }

    @Test
    void cancelSalewhenUnauthorizedthrowsUnauthorizedException() {
        String cartIdStr = testCartId.toHexString();
        Long unauthorizedUserId = 999L;

        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.PREPARADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));
        testProduct.setCreatorId(testUserId);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.cancelSale(cartIdStr, testProductId, unauthorizedUserId, false))
                .isInstanceOf(CartException.UnauthorizedException.class);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
        verify(productRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void cancelSalewhenAlreadyCanceleddoesNotRestoreStockAgain() {
        String cartIdStr = testCartId.toHexString();
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(3)
                .status(Status.CANCELADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        cartService.cancelSale(cartIdStr, testProductId, testUserId, true);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
        verify(productRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void updatewhenLineExistsupdatesStatusSuccessfully() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.EN_CARRITO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        dev.luisvives.dawazon.cart.dto.LineRequestDto lineRequestDto = new dev.luisvives.dawazon.cart.dto.LineRequestDto(
                testCartId, testProductId, Status.ENVIADO);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        Cart result = cartService.update(lineRequestDto);

        assertThat(result).isEqualTo(testCart);
        verify(cartRepository).updateCartLineStatus(testCartId, testProductId, Status.ENVIADO);
        verify(cartRepository).findById(testCartId);
    }

    @Test
    void updatewhenCartNotFoundthrowsCartNotFoundException() {
        dev.luisvives.dawazon.cart.dto.LineRequestDto lineRequestDto = new dev.luisvives.dawazon.cart.dto.LineRequestDto(
                testCartId, testProductId, Status.ENVIADO);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.update(lineRequestDto))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).updateCartLineStatus(testCartId, testProductId, Status.ENVIADO);
        verify(cartRepository).findById(testCartId);
    }

    @Test
    void updateStockwhenCartAndLineExistupdatesQuantitySuccessfully() {
        CartLine existingLine = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(existingLine)));

        CartStockRequestDto requestDto = new CartStockRequestDto();
        requestDto.setCartId(testCartId);
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(5);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.updateStock(requestDto);

        assertThat(result).isEqualTo(testCart);
        assertThat(existingLine.getQuantity()).isEqualTo(5);
        verify(cartRepository).findById(testCartId);
        verify(cartRepository).save(testCart);
    }

    @Test
    void updateStockwhenCartNotFoundthrowsCartNotFoundException() {
        CartStockRequestDto requestDto = new CartStockRequestDto();
        requestDto.setCartId(testCartId);
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(5);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateStock(requestDto))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void getSaleLineByIdswhenAdminRequestsreturnsSaleLineDto() {
        String cartIdStr = testCartId.toHexString();
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.ENVIADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        dev.luisvives.dawazon.cart.dto.SaleLineDto expectedDto = new dev.luisvives.dawazon.cart.dto.SaleLineDto();

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartMapper.cartlineToSaleLineDto(any(), any(), any(), any())).thenReturn(expectedDto);

        dev.luisvives.dawazon.cart.dto.SaleLineDto result = cartService.getSaleLineByIds(cartIdStr, testProductId,
                testUserId, true);

        assertThat(result).isEqualTo(expectedDto);
        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void getSaleLineByIdswhenManagerOwnsProductreturnsSaleLineDto() {
        String cartIdStr = testCartId.toHexString();
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.ENVIADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));
        testProduct.setCreatorId(testUserId);

        dev.luisvives.dawazon.cart.dto.SaleLineDto expectedDto = new dev.luisvives.dawazon.cart.dto.SaleLineDto();

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartMapper.cartlineToSaleLineDto(any(), any(), any(), any())).thenReturn(expectedDto);

        dev.luisvives.dawazon.cart.dto.SaleLineDto result = cartService.getSaleLineByIds(cartIdStr, testProductId,
                testUserId, false);

        assertThat(result).isEqualTo(expectedDto);
        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
    }

    @Test
    void getSaleLineByIdswhenUnauthorizedthrowsUnauthorizedException() {
        String cartIdStr = testCartId.toHexString();
        Long unauthorizedUserId = 999L;

        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.ENVIADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));
        testProduct.setCreatorId(testUserId);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.getSaleLineByIds(cartIdStr, testProductId, unauthorizedUserId, false))
                .isInstanceOf(CartException.UnauthorizedException.class);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
    }

    @Test
    void getSaleLineByIdswhenCartNotFoundthrowsCartNotFoundException() {
        String cartIdStr = testCartId.toHexString();

        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getSaleLineByIds(cartIdStr, testProductId, testUserId, true))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void getSaleLineByIdswhenLineNotFoundthrowsCartNotFoundException() {
        String cartIdStr = testCartId.toHexString();
        String wrongProductId = "WRONG-PRODUCT";

        testCart.setCartLines(new ArrayList<>());

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.getSaleLineByIds(cartIdStr, wrongProductId, testUserId, true))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void findAllwhenNoFiltersreturnsAllCarts() {
        List<Cart> carts = List.of(testCart);
        long count = 1;

        when(mongoTemplate.count(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(count);
        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(carts);

        org.springframework.data.domain.Page<Cart> result = cartService.findAll(
                Optional.empty(),
                Optional.empty(),
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(mongoTemplate).count(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void findAllwhenFilterByUserIdreturnsUserCarts() {
        List<Cart> carts = List.of(testCart);
        long count = 1;

        when(mongoTemplate.count(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(count);
        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(carts);

        org.springframework.data.domain.Page<Cart> result = cartService.findAll(
                Optional.of(testUserId),
                Optional.empty(),
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(mongoTemplate).count(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void findAllwhenFilterByPurchasedreturnsPurchasedCarts() {
        testCart.setPurchased(true);
        List<Cart> carts = List.of(testCart);
        long count = 1;

        when(mongoTemplate.count(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(count);
        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(carts);

        org.springframework.data.domain.Page<Cart> result = cartService.findAll(
                Optional.empty(),
                Optional.of("true"),
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(mongoTemplate).count(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void calculateTotalEarningswhenAdminWithNoManagerreturnsAllEarnings() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        testCart.setPurchased(true);
        testCart.setCartLines(List.of(line));
        List<Cart> carts = List.of(testCart);

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(carts);

        Double result = cartService.calculateTotalEarnings(Optional.empty(), true);

        assertThat(result).isEqualTo(100.0);
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void calculateTotalEarningswhenManagerWithProductsreturnsManagerEarnings() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        testCart.setPurchased(true);
        testCart.setCartLines(List.of(line));
        List<Cart> carts = List.of(testCart);

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(carts);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        Double result = cartService.calculateTotalEarnings(Optional.of(testUserId), false);

        assertThat(result).isEqualTo(100.0);
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void cleanupExpiredCheckoutswhenNoExpiredCartsreturnsZero() {
        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(List.of());

        int result = cartService.cleanupExpiredCheckouts();

        assertThat(result).isZero();
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void cleanupExpiredCheckoutswhenExpiredCartExistsrestoresStockAndReturnsCount() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(3)
                .build();

        testCart.setCheckoutInProgress(true);
        testCart.setCheckoutStartedAt(java.time.LocalDateTime.now().minusMinutes(10));
        testCart.setCartLines(List.of(line));
        testCart.setPurchased(false);

        List<Cart> expiredCarts = List.of(testCart);

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(expiredCarts);
        when(cartRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        int result = cartService.cleanupExpiredCheckouts();

        assertThat(result).isEqualTo(1);
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
        verify(cartRepository).findById(any(ObjectId.class));
    }

    @Test
    void sendConfirmationEmailAsyncwhenCalledstartsThreadSuccessfully() throws InterruptedException {
        cartService.sendConfirmationEmailAsync(testCart);

        Thread.sleep(100);

    }

    @Test
    void findAllSalesAsLineswhenAdminWithNoFilterreturnsAllSalesLines() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.PREPARADO)
                .build();

        testCart.setPurchased(true);
        testCart.setCartLines(List.of(line));

        dev.luisvives.dawazon.cart.dto.SaleLineDto saleLineDto = new dev.luisvives.dawazon.cart.dto.SaleLineDto();

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(List.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartMapper.cartlineToSaleLineDto(any(), any(), any(), any())).thenReturn(saleLineDto);

        org.springframework.data.domain.Page<dev.luisvives.dawazon.cart.dto.SaleLineDto> result = cartService
                .findAllSalesAsLines(
                        Optional.empty(),
                        true,
                        org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void findAllSalesAsLineswhenManagerFilteredNotAdminreturnsOnlyManagerSales() {
        CartLine line1 = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.PREPARADO)
                .build();

        testCart.setPurchased(true);
        testCart.setCartLines(List.of(line1));

        dev.luisvives.dawazon.cart.dto.SaleLineDto saleLineDto = new dev.luisvives.dawazon.cart.dto.SaleLineDto();
        saleLineDto.setManagerId(testUserId);

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(List.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartMapper.cartlineToSaleLineDto(any(), any(), any(), any())).thenReturn(saleLineDto);

        org.springframework.data.domain.Page<dev.luisvives.dawazon.cart.dto.SaleLineDto> result = cartService
                .findAllSalesAsLines(
                        Optional.of(testUserId),
                        false,
                        org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void findAllSalesAsLineswhenErrorProcessingLineskipsLineAndContinues() {
        CartLine line1 = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.PREPARADO)
                .build();

        CartLine line2 = CartLine.builder()
                .productId("MISSING-PRODUCT")
                .quantity(1)
                .status(Status.ENVIADO)
                .build();

        testCart.setPurchased(true);
        testCart.setCartLines(List.of(line1, line2));

        dev.luisvives.dawazon.cart.dto.SaleLineDto saleLineDto = new dev.luisvives.dawazon.cart.dto.SaleLineDto();

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(List.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(productRepository.findById("MISSING-PRODUCT")).thenReturn(Optional.empty());
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartMapper.cartlineToSaleLineDto(any(), any(), any(), any())).thenReturn(saleLineDto);

        org.springframework.data.domain.Page<dev.luisvives.dawazon.cart.dto.SaleLineDto> result = cartService
                .findAllSalesAsLines(
                        Optional.empty(),
                        true,
                        org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void calculateTotalEarningswhenManagerNotAdminWithoutManagerIdreturnsZero() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        testCart.setPurchased(true);
        testCart.setCartLines(List.of(line));
        List<Cart> carts = List.of(testCart);

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(carts);

        Double result = cartService.calculateTotalEarnings(Optional.empty(), false);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void getSaleLineByIdswhenUserNotFoundthrowsUserNotFoundException() {
        String cartIdStr = testCartId.toHexString();
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.ENVIADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getSaleLineByIds(cartIdStr, testProductId, testUserId, true))
                .isInstanceOf(dev.luisvives.dawazon.users.exceptions.UserException.UserNotFoundException.class);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
    }

    @Test
    void checkoutwhenUserNotFoundthrowsUserNotFoundException() {
        testCart.setCartLines(new ArrayList<>());

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.checkout(testCartId, testCart))
                .isInstanceOf(dev.luisvives.dawazon.users.exceptions.UserException.UserNotFoundException.class);

        verify(userRepository).findById(testUserId);
    }

    @Test
    void cleanupExpiredCheckoutswhenExceptionDuringCleanuplogsErrorAndContinues() {
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(3)
                .build();

        testCart.setCheckoutInProgress(true);
        testCart.setCheckoutStartedAt(java.time.LocalDateTime.now().minusMinutes(10));
        testCart.setCartLines(List.of(line));
        testCart.setPurchased(false);

        List<Cart> expiredCarts = List.of(testCart);

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(expiredCarts);
        when(cartRepository.findById(any(ObjectId.class)))
                .thenThrow(new RuntimeException("Database error"));

        int result = cartService.cleanupExpiredCheckouts();

        assertThat(result).isZero();
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void findAllSalesAsLineswhenPaginationBeyondSizereturnsEmptyList() {
        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(List.of());

        org.springframework.data.domain.Page<dev.luisvives.dawazon.cart.dto.SaleLineDto> result = cartService
                .findAllSalesAsLines(
                        Optional.empty(),
                        true,
                        org.springframework.data.domain.PageRequest.of(10, 10)
                );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void calculateTotalEarningswhenProductNotFoundReturnsNullfiltersOut() {
        CartLine line1 = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        CartLine line2 = CartLine.builder()
                .productId("MISSING-PRODUCT")
                .quantity(1)
                .productPrice(25.0)
                .totalPrice(25.0)
                .build();

        testCart.setPurchased(true);
        testCart.setCartLines(List.of(line1, line2));
        List<Cart> carts = List.of(testCart);

        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(carts);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(productRepository.findById("MISSING-PRODUCT")).thenReturn(Optional.empty());

        Double result = cartService.calculateTotalEarnings(Optional.of(testUserId), false);

        assertThat(result).isEqualTo(100.0);
    }

    @Test
    void sendConfirmationEmailAsyncwhenEmailServiceThrowsExceptionlogsWarning() throws InterruptedException {
        doThrow(new RuntimeException("Email service error"))
                .when(orderEmailService).enviarConfirmacionPedidoHtml(any(Cart.class));

        cartService.sendConfirmationEmailAsync(testCart);

        Thread.sleep(200);

        verify(orderEmailService).enviarConfirmacionPedidoHtml(testCart);
    }

    @Test
    void getSaleLineByIdswhenProductNotFoundthrowsProductNotFoundException() {
        String cartIdStr = testCartId.toHexString();
        CartLine line = CartLine.builder()
                .productId("MISSING-PRODUCT")
                .quantity(2)
                .status(Status.ENVIADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById("MISSING-PRODUCT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getSaleLineByIds(cartIdStr, "MISSING-PRODUCT", testUserId, true))
                .isInstanceOf(ProductException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById("MISSING-PRODUCT");
    }

    @Test
    void updateStockWithValidationwhenCartNotFoundthrowsCartNotFoundException() {
        CartStockRequestDto requestDto = new CartStockRequestDto();
        requestDto.setCartId(testCartId);
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(5);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateStockWithValidation(requestDto))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void checkoutwhenProductNotFoundInLoopthrowsProductNotFoundException() {
        CartLine line = CartLine.builder()
                .productId("MISSING-PRODUCT")
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productRepository.findById("MISSING-PRODUCT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.checkout(testCartId, testCart))
                .isInstanceOf(ProductException.NotFoundException.class);

        verify(productRepository).findById("MISSING-PRODUCT");
    }

    @Test
    void restoreStockwhenCartNotFoundthrowsRuntimeException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.restoreStock(testCartId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Carrito no encontrado");

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void cancelSalewhenCartNotFoundthrowsCartNotFoundException() {
        String cartIdStr = testCartId.toHexString();

        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.cancelSale(cartIdStr, testProductId, testUserId, true))
                .isInstanceOf(CartException.NotFoundException.class)
                .hasMessageContaining("Venta no encontrada");

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void cancelSalewhenProductLineNotFoundthrowsCartNotFoundException() {
        String cartIdStr = testCartId.toHexString();
        testCart.setCartLines(new ArrayList<>());

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.cancelSale(cartIdStr, testProductId, testUserId, true))
                .isInstanceOf(CartException.NotFoundException.class)
                .hasMessageContaining("Producto no encontrado en esta venta");

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void cancelSalewhenProductNotFoundthrowsProductNotFoundException() {
        String cartIdStr = testCartId.toHexString();
        CartLine line = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .status(Status.PREPARADO)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(line)));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.cancelSale(cartIdStr, testProductId, testUserId, true))
                .isInstanceOf(ProductException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
        verify(productRepository).findById(testProductId);
    }

    @Test
    void removeProductwhenSecondFindByIdFailsthrowsCartNotFoundException() {
        CartLine existingLine = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(existingLine)));

        when(cartRepository.findById(testCartId))
                .thenReturn(Optional.of(testCart))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeProduct(testCartId, testProductId))
                .isInstanceOf(CartException.NotFoundException.class)
                .hasMessageContaining("Cart no encontrado con id: " + testCartId);

        verify(cartRepository, times(2)).findById(testCartId);
        verify(cartRepository).removeCartLine(eq(testCartId), any(CartLine.class));
    }
}
