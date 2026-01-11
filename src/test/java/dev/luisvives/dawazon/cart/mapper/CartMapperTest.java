package dev.luisvives.dawazon.cart.mapper;

import dev.luisvives.dawazon.cart.dto.SaleLineDto;
import dev.luisvives.dawazon.cart.models.*;
import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.users.models.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests para CartMapper siguiendo principios FIRST:
 * - Fast: Sin dependencias externas (base de datos, contenedores)
 * - Isolated: Cada test es independiente
 * - Repeatable: Resultados consistentes
 * - Self-validating: Aserciones claras
 * - Timely: Cobertura completa
 */
@DisplayName("CartMapper Unit Tests - FIRST Principles")
class CartMapperTest {

    private CartMapper cartMapper;

    // Test data builders
    private Cart cart;
    private Product product;
    private CartLine cartLine;
    private User manager;
    private Client client;
    private Address address;

    // Test constants
    private static final Long USER_ID = 123L;
    private static final Long PRODUCT_CREATOR_ID = 456L;
    private static final String PRODUCT_ID = "PROD-001";
    private static final String PRODUCT_NAME = "Test Product";
    private static final Double PRODUCT_PRICE = 99.99;
    private static final Integer QUANTITY = 3;
    private static final Double TOTAL_PRICE = 299.97;
    private static final String MANAGER_USERNAME = "vendedor123";

    // Client data
    private static final String CLIENT_NAME = "Juan Pérez";
    private static final String CLIENT_EMAIL = "juan@example.com";
    private static final String CLIENT_PHONE = "+34 600 123 456";

    // Address data
    private static final String ADDRESS_STREET = "Calle Principal";
    private static final Short ADDRESS_NUMBER = 123;
    private static final String ADDRESS_CITY = "Madrid";
    private static final String ADDRESS_PROVINCE = "Madrid";
    private static final String ADDRESS_COUNTRY = "España";
    private static final Integer ADDRESS_POSTAL_CODE = 28001;

    @BeforeEach
    void setUp() {
        // Principio FIRST: Isolated - Cada test comienza con estado limpio
        cartMapper = new CartMapper();

        // Crear objetos de prueba
        address = Address.builder()
                .street(ADDRESS_STREET)
                .number(ADDRESS_NUMBER)
                .city(ADDRESS_CITY)
                .province(ADDRESS_PROVINCE)
                .country(ADDRESS_COUNTRY)
                .postalCode(ADDRESS_POSTAL_CODE)
                .build();

        client = Client.builder()
                .name(CLIENT_NAME)
                .email(CLIENT_EMAIL)
                .phone(CLIENT_PHONE)
                .address(address)
                .build();

        cart = Cart.builder()
                .id(new ObjectId())
                .userId(USER_ID)
                .client(client)
                .purchased(false)
                .cartLines(new ArrayList<>())
                .totalItems(1)
                .total(TOTAL_PRICE)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 11, 45))
                .build();

        product = Product.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .price(PRODUCT_PRICE)
                .creatorId(PRODUCT_CREATOR_ID)
                .stock(100)
                .description("Descripción del producto")
                .images(new ArrayList<>())
                .isDeleted(false)
                .build();

        cartLine = CartLine.builder()
                .productId(PRODUCT_ID)
                .quantity(QUANTITY)
                .productPrice(PRODUCT_PRICE)
                .totalPrice(TOTAL_PRICE)
                .status(Status.EN_CARRITO)
                .build();

        manager = User.builder()
                .id(PRODUCT_CREATOR_ID)
                .userName(MANAGER_USERNAME)
                .email("vendedor@example.com")
                .password("encrypted-password")
                .build();
    }

    @Test
    @DisplayName("FIRST: Complete mapping - Debe mapear todos los campos correctamente")
    void testCartlineToSaleLineDto_CompleteMapping() {
        // Principio FIRST: Self-validating - Aserciones claras y explícitas

        // When
        SaleLineDto result = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then - Verificar TODOS los campos
        assertThat(result).isNotNull();

        // Campos derivados del Cart
        assertThat(result.getSaleId()).isEqualTo(cart.getId());
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getClient()).isEqualTo(client);
        assertThat(result.getCreatedAt()).isEqualTo(cart.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(cart.getUpdatedAt());

        // Campos derivados del Product
        assertThat(result.getProductId()).isEqualTo(String.valueOf(PRODUCT_ID));
        assertThat(result.getProductName()).isEqualTo(PRODUCT_NAME);
        assertThat(result.getManagerId()).isEqualTo(PRODUCT_CREATOR_ID);

        // Campos derivados de CartLine
        assertThat(result.getQuantity()).isEqualTo(QUANTITY);
        assertThat(result.getProductPrice()).isEqualTo(PRODUCT_PRICE);
        assertThat(result.getTotalPrice()).isEqualTo(TOTAL_PRICE);
        assertThat(result.getStatus()).isEqualTo(Status.EN_CARRITO);

        // Campos derivados de Manager
        assertThat(result.getManagerName()).isEqualTo(MANAGER_USERNAME);
    }

    @Test
    @DisplayName("FIRST: Type conversions - Debe convertir ObjectId a String correctamente")
    void testCartlineToSaleLineDto_TypeConversions() {
        // Principio FIRST: Repeatable - Mismo resultado cada vez

        // Given
        ObjectId cartId = new ObjectId();
        cart.setId(cartId);

        // When
        SaleLineDto result = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then
        assertThat(result.getSaleId())
                .isNotNull()
                .isEqualTo(cartId.toHexString())
                .matches("^[0-9a-f]{24}$"); // Validar formato ObjectId hex

        // Verificar conversión de Product ID
        assertThat(result.getProductId())
                .isNotNull()
                .isEqualTo(String.valueOf(PRODUCT_ID));
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    @DisplayName("FIRST: Status enum - Debe mapear todos los estados de Status correctamente")
    void testCartlineToSaleLineDto_AllStatusValues(Status status) {
        // Principio FIRST: Repeatable - Probar todos los valores del enum

        // Given
        cartLine.setStatus(status);

        // When
        SaleLineDto result = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then
        assertThat(result.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("FIRST: Price integrity - Debe mantener integridad de precios")
    void testCartlineToSaleLineDto_PriceIntegrity() {
        // Principio FIRST: Self-validating - Verificar cálculos

        // Given - Diferentes combinaciones de precios
        Double customPrice = 49.99;
        Integer customQuantity = 5;
        Double customTotal = customPrice * customQuantity;

        cartLine.setProductPrice(customPrice);
        cartLine.setQuantity(customQuantity);
        cartLine.setTotalPrice(customTotal);

        // When
        SaleLineDto result = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then
        assertThat(result.getProductPrice()).isEqualTo(customPrice);
        assertThat(result.getTotalPrice()).isEqualTo(customTotal);
        assertThat(result.getQuantity()).isEqualTo(customQuantity);

        // Verificar consistencia (aunque el mapper no calcula, validamos los valores)
        assertThat(result.getTotalPrice())
                .isEqualTo(result.getProductPrice() * result.getQuantity());
    }

    @Test
    @DisplayName("FIRST: Price edge cases - Debe manejar precios cero correctamente")
    void testCartlineToSaleLineDto_ZeroPrices() {
        // Given
        cartLine.setProductPrice(0.0);
        cartLine.setQuantity(0);
        cartLine.setTotalPrice(0.0);

        // When
        SaleLineDto result = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then
        assertThat(result.getProductPrice()).isZero();
        assertThat(result.getTotalPrice()).isZero();
        assertThat(result.getQuantity()).isZero();
    }

    @Test
    @DisplayName("FIRST: Timestamps - Debe copiar timestamps correctamente")
    void testCartlineToSaleLineDto_Timestamps() {
        // Given
        LocalDateTime createdTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45);
        LocalDateTime updatedTime = LocalDateTime.of(2024, 6, 16, 9, 15, 20);

        cart.setCreatedAt(createdTime);
        cart.setUpdatedAt(updatedTime);

        // When
        SaleLineDto result = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then
        assertThat(result.getCreatedAt()).isEqualTo(createdTime);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedTime);
        assertThat(result.getUpdatedAt()).isAfter(result.getCreatedAt());
    }

    @Test
    @DisplayName("FIRST: Client data - Debe copiar todos los datos del cliente")
    void testCartlineToSaleLineDto_ClientData() {
        // When
        SaleLineDto result = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then
        assertThat(result.getClient()).isNotNull();
        assertThat(result.getClient()).isEqualTo(client);

        // Verificar métodos helper del DTO
        assertThat(result.getUserName()).isEqualTo(CLIENT_NAME);
        assertThat(result.getUserEmail()).isEqualTo(CLIENT_EMAIL);

        // Verificar datos del cliente
        assertThat(result.getClient().getName()).isEqualTo(CLIENT_NAME);
        assertThat(result.getClient().getEmail()).isEqualTo(CLIENT_EMAIL);
        assertThat(result.getClient().getPhone()).isEqualTo(CLIENT_PHONE);

        // Verificar dirección del cliente
        assertThat(result.getClient().getAddress()).isNotNull();
        assertThat(result.getClient().getAddress().getStreet()).isEqualTo(ADDRESS_STREET);
        assertThat(result.getClient().getAddress().getCity()).isEqualTo(ADDRESS_CITY);
    }

    @Test
    @DisplayName("FIRST: Isolated - Tests no se afectan entre sí")
    void testCartlineToSaleLineDto_Isolation() {
        // Principio FIRST: Isolated - Múltiples llamadas no se afectan

        // Primera llamada
        SaleLineDto result1 = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Modificar datos
        cartLine.setQuantity(10);
        cartLine.setTotalPrice(999.99);

        // Segunda llamada
        SaleLineDto result2 = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then - Los resultados son independientes
        assertThat(result1.getQuantity()).isEqualTo(QUANTITY); // Valor original
        assertThat(result2.getQuantity()).isEqualTo(10); // Valor modificado
        assertThat(result1.getTotalPrice()).isEqualTo(TOTAL_PRICE);
        assertThat(result2.getTotalPrice()).isEqualTo(999.99);
    }

    @Test
    @DisplayName("Debe obtener correctamente datos del manager")
    void testCartlineToSaleLineDto_ManagerData() {
        // When
        SaleLineDto result = cartMapper.cartlineToSaleLineDto(cart, product, cartLine, manager);

        // Then
        assertThat(result.getManagerId()).isEqualTo(product.getCreatorId());
        assertThat(result.getManagerName()).isEqualTo(manager.getUsername());

        // Verificar que el managerId viene del Product, no del User
        assertThat(result.getManagerId()).isEqualTo(PRODUCT_CREATOR_ID);
    }
}
