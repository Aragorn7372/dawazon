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
    void addProduct_whenProductAndCartExist_addsProductSuccessfully() {
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
    void addProduct_whenProductNotFound_throwsProductNotFoundException() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProduct(testCartId, testProductId))
                .isInstanceOf(ProductException.NotFoundException.class);

        verify(productRepository).findById(testProductId);
        verify(cartRepository, never()).addCartLine(any(), any());
    }

    @Test
    void addProduct_whenCartNotFound_throwsCartNotFoundException() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProduct(testCartId, testProductId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(productRepository).findById(testProductId);
        verify(cartRepository).addCartLine(any(), any());
        verify(cartRepository).findById(testCartId);
    }

    @Test
    void removeProduct_whenProductInCart_removesSuccessfully() {
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
    void removeProduct_whenCartNotFound_throwsCartNotFoundException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeProduct(testCartId, testProductId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
        verify(cartRepository, never()).removeCartLine(any(), any());
    }

    @Test
    void getById_whenCartExists_returnsCart() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        Cart result = cartService.getById(testCartId);

        assertThat(result).isEqualTo(testCart);
        verify(cartRepository).findById(testCartId);
    }

    @Test
    void getById_whenCartNotFound_throwsCartNotFoundException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getById(testCartId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void save_whenCartSaved_marksAsPurchasedAndCreatesNewCart() {
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
    void save_whenCartSaved_setsAllLinesToPreparado() {
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
    void updateStockWithValidation_whenValidQuantity_updatesSuccessfully() {
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
    void updateStockWithValidation_whenQuantityLessThanOne_throwsIllegalArgumentException() {
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
    void updateStockWithValidation_whenInsufficientStock_throwsInsufficientStockException() {
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
    void updateStockWithValidation_whenProductNotFound_throwsProductNotFoundException() {
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
    void createNewCart_whenUserExists_createsNewCart() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.createNewCart(testUserId);

        assertThat(result).isNotNull();
        verify(userRepository).findById(testUserId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void checkout_whenStockAvailable_processesCheckoutSuccessfully() {
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
    void checkout_whenInsufficientStock_throwsProductQuantityExceededException() {
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
    void restoreStock_whenCartNotPurchased_restoresStock() {
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
    void restoreStock_whenCartPurchased_doesNotRestoreStock() {
        testCart.setPurchased(true);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        cartService.restoreStock(testCartId);

        verify(cartRepository).findById(testCartId);
        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void deleteById_whenCartExists_emptiesCart() {
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
    void deleteById_whenCartNotFound_throwsCartNotFoundException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteById(testCartId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void variosPorId_whenAllProductsExist_returnsAllProducts() {
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
    void variosPorId_whenProductNotFound_throwsProductNotFoundException() {
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
    void getCartByUserId_whenUserHasActiveCart_returnsCart() {
        when(cartRepository.findByUserIdAndPurchased(testUserId, false))
                .thenReturn(Optional.of(testCart));

        Cart result = cartService.getCartByUserId(testUserId);

        assertThat(result).isEqualTo(testCart);
        verify(cartRepository).findByUserIdAndPurchased(testUserId, false);
    }

    @Test
    void getCartByUserId_whenNoActiveCart_throwsCartNotFoundException() {
        when(cartRepository.findByUserIdAndPurchased(testUserId, false))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCartByUserId(testUserId))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findByUserIdAndPurchased(testUserId, false);
    }

    @Test
    void cancelSale_whenAdminCancels_cancelsSuccessfully() {
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
    void cancelSale_whenManagerOwnsProduct_cancelsSuccessfully() {
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
    void cancelSale_whenUnauthorized_throwsUnauthorizedException() {
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
    void cancelSale_whenAlreadyCanceled_doesNotRestoreStockAgain() {
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
    void update_whenLineExists_updatesStatusSuccessfully() {
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
    void update_whenCartNotFound_throwsCartNotFoundException() {
        dev.luisvives.dawazon.cart.dto.LineRequestDto lineRequestDto = new dev.luisvives.dawazon.cart.dto.LineRequestDto(
                testCartId, testProductId, Status.ENVIADO);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.update(lineRequestDto))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).updateCartLineStatus(testCartId, testProductId, Status.ENVIADO);
        verify(cartRepository).findById(testCartId);
    }

    @Test
    void updateStock_whenCartAndLineExist_updatesQuantitySuccessfully() {
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
    void updateStock_whenCartNotFound_throwsCartNotFoundException() {
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
    void getSaleLineByIds_whenAdminRequests_returnsSaleLineDto() {
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
    void getSaleLineByIds_whenManagerOwnsProduct_returnsSaleLineDto() {
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
    void getSaleLineByIds_whenUnauthorized_throwsUnauthorizedException() {
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
    void getSaleLineByIds_whenCartNotFound_throwsCartNotFoundException() {
        String cartIdStr = testCartId.toHexString();

        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getSaleLineByIds(cartIdStr, testProductId, testUserId, true))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void getSaleLineByIds_whenLineNotFound_throwsCartNotFoundException() {
        String cartIdStr = testCartId.toHexString();
        String wrongProductId = "WRONG-PRODUCT";

        testCart.setCartLines(new ArrayList<>());

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.getSaleLineByIds(cartIdStr, wrongProductId, testUserId, true))
                .isInstanceOf(CartException.NotFoundException.class);

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void findAll_whenNoFilters_returnsAllCarts() {
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
    void findAll_whenFilterByUserId_returnsUserCarts() {
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
    void findAll_whenFilterByPurchased_returnsPurchasedCarts() {
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
    void calculateTotalEarnings_whenAdminWithNoManager_returnsAllEarnings() {
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
    void calculateTotalEarnings_whenManagerWithProducts_returnsManagerEarnings() {
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
    void cleanupExpiredCheckouts_whenNoExpiredCarts_returnsZero() {
        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(List.of());

        int result = cartService.cleanupExpiredCheckouts();

        assertThat(result).isZero();
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void cleanupExpiredCheckouts_whenExpiredCartExists_restoresStockAndReturnsCount() {
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
    void sendConfirmationEmailAsync_whenCalled_startsThreadSuccessfully() throws InterruptedException {
        cartService.sendConfirmationEmailAsync(testCart);

        Thread.sleep(100);

        // No verification needed, just ensure it doesn't throw an exception
        // The method creates a daemon thread and returns immediately
    }

    @Test
    void findAllSalesAsLines_whenAdminWithNoFilter_returnsAllSalesLines() {
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
    void findAllSalesAsLines_whenManagerFilteredNotAdmin_returnsOnlyManagerSales() {
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
    void findAllSalesAsLines_whenErrorProcessingLine_skipsLineAndContinues() {
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

        // Should only have 1 line (the one that succeeded)
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void calculateTotalEarnings_whenManagerNotAdminWithoutManagerId_returnsZero() {
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
    void getSaleLineByIds_whenUserNotFound_throwsUserNotFoundException() {
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
    void checkout_whenUserNotFound_throwsUserNotFoundException() {
        testCart.setCartLines(new ArrayList<>());

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.checkout(testCartId, testCart))
                .isInstanceOf(dev.luisvives.dawazon.users.exceptions.UserException.UserNotFoundException.class);

        verify(userRepository).findById(testUserId);
    }

    @Test
    void cleanupExpiredCheckouts_whenExceptionDuringCleanup_logsErrorAndContinues() {
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

        assertThat(result).isZero(); // Should be 0 because the cleanup failed
        verify(mongoTemplate).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class));
    }

    @Test
    void findAllSalesAsLines_whenPaginationBeyondSize_returnsEmptyList() {
        // Empty cart list - ensures pagination returns empty when start >= size
        when(mongoTemplate.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Cart.class)))
                .thenReturn(List.of());

        org.springframework.data.domain.Page<dev.luisvives.dawazon.cart.dto.SaleLineDto> result = cartService
                .findAllSalesAsLines(
                        Optional.empty(),
                        true,
                        org.springframework.data.domain.PageRequest.of(10, 10) // Page 10, way beyond the data
                );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void calculateTotalEarnings_whenProductNotFoundReturnsNull_filtersOut() {
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

        // Should only count the first product (100.0), second one is filtered out as
        // null
        assertThat(result).isEqualTo(100.0);
    }

    @Test
    void sendConfirmationEmailAsync_whenEmailServiceThrowsException_logsWarning() throws InterruptedException {
        doThrow(new RuntimeException("Email service error"))
                .when(orderEmailService).enviarConfirmacionPedidoHtml(any(Cart.class));

        // Should not throw exception, just log warning
        cartService.sendConfirmationEmailAsync(testCart);

        // Wait for async thread to complete
        Thread.sleep(200);

        // No exception should be thrown, method completes successfully
        verify(orderEmailService).enviarConfirmacionPedidoHtml(testCart);
    }

    @Test
    void getSaleLineByIds_whenProductNotFound_throwsProductNotFoundException() {
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
    void updateStockWithValidation_whenCartNotFound_throwsCartNotFoundException() {
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
    void checkout_whenProductNotFoundInLoop_throwsProductNotFoundException() {
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
    void restoreStock_whenCartNotFound_throwsRuntimeException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.restoreStock(testCartId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Carrito no encontrado");

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void cancelSale_whenCartNotFound_throwsCartNotFoundException() {
        String cartIdStr = testCartId.toHexString();

        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.cancelSale(cartIdStr, testProductId, testUserId, true))
                .isInstanceOf(CartException.NotFoundException.class)
                .hasMessageContaining("Venta no encontrada");

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void cancelSale_whenProductLineNotFound_throwsCartNotFoundException() {
        String cartIdStr = testCartId.toHexString();
        testCart.setCartLines(new ArrayList<>()); // Empty lines

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.cancelSale(cartIdStr, testProductId, testUserId, true))
                .isInstanceOf(CartException.NotFoundException.class)
                .hasMessageContaining("Producto no encontrado en esta venta");

        verify(cartRepository).findById(testCartId);
    }

    @Test
    void cancelSale_whenProductNotFound_throwsProductNotFoundException() {
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
    void removeProduct_whenSecondFindByIdFails_throwsCartNotFoundException() {
        CartLine existingLine = CartLine.builder()
                .productId(testProductId)
                .quantity(2)
                .productPrice(50.0)
                .totalPrice(100.0)
                .build();

        testCart.setCartLines(new ArrayList<>(List.of(existingLine)));

        // First findById succeeds, second one fails
        when(cartRepository.findById(testCartId))
                .thenReturn(Optional.of(testCart)) // First call succeeds
                .thenReturn(Optional.empty()); // Second call fails

        assertThatThrownBy(() -> cartService.removeProduct(testCartId, testProductId))
                .isInstanceOf(CartException.NotFoundException.class)
                .hasMessageContaining("Cart no encontrado con id: " + testCartId);

        verify(cartRepository, times(2)).findById(testCartId);
        verify(cartRepository).removeCartLine(eq(testCartId), any(CartLine.class));
    }
}
