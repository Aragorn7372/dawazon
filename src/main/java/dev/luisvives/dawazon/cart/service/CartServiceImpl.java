package dev.luisvives.dawazon.cart.service;

import dev.luisvives.dawazon.cart.exceptions.CartException;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.cart.models.Status;
import dev.luisvives.dawazon.cart.repository.CartRepository;
import dev.luisvives.dawazon.common.dto.PageResponseDTO;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.products.models.Category;
import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.stripe.service.StripeService;
import dev.luisvives.dawazon.users.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.bson.types.ObjectId;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private StripeService stripeService;
    private UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public CartServiceImpl(ProductRepository productRepository, CartRepository cartRepository, UserRepository userRepository, StripeService stripeService, MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.stripeService = stripeService;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public Page<Cart> findAll(Optional<Long> userId,
                              Optional<String> purchased,
                              Pageable pageable) {
        // 1. Crear la Query de Mongo
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        // 2. Filtro por User ID (si está presente)
        userId.ifPresent(id ->
                criteriaList.add(Criteria.where("userId").is(id))
        );
        // 3. Filtro por Estado de Compra (purchased)
        // El input es String ("true"/"false"), pero en BD es boolean. Hacemos el parseo.
        purchased.ifPresent(p -> {
            boolean isPurchased = Boolean.parseBoolean(p);
            criteriaList.add(Criteria.where("purchased").is(isPurchased));
        });
        // 4. Aplicar los criterios
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        // 5. Contar el total de elementos (necesario para el objeto Page)
        // Esto se hace ANTES de aplicar la paginación a la query
        long count = mongoTemplate.count(query, Cart.class);
        // 6. Aplicar paginación y ordenación
        query.with(pageable);
        // 7. Ejecutar la búsqueda
        List<Cart> carts = mongoTemplate.find(query, Cart.class);
        // 8. Retornar la página
        return new PageImpl<>(carts, pageable, count);
    }

    @Override
    @Transactional
    public Cart addProduct(ObjectId id, String productId) {
        log.info("Adding product " + productId + " to " + id);
        // Comprobamos que la cantidad de producto este en stock
        val product=productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Producto no encontrado con id: " + productId);
            return new ProductException.NotFoundException(productId);
        });
        val line= new CartLine().builder()
                .quantity(1)
                .productPrice(product.getPrice())
                .productId(productId)
                .status(Status.EN_CARRITO)
                .build();
        return cartRepository.addCartLine(id,line).orElseThrow(()->{
            log.warn("Cart no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        });
    }

    @Override
    @Transactional
    public Cart removeProduct(ObjectId id, String productId) {
        log.info("Removing product with id from cart: " + productId);
        val cartLine=cartRepository.findById(id).orElseThrow(()->{
            log.warn("Cart no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        }).getCartLines().stream().filter((it)->it.getProductId().equals(productId)).findFirst().get();
        val line= new CartLine().builder()
                .quantity(cartLine.getQuantity())
                .productPrice(cartLine.getProductPrice())
                .productId(productId)
                .status(Status.EN_CARRITO)
                .build();
        return cartRepository.removeCartLine(id,line).orElseThrow(()->{
            log.warn("Cart no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        });
    }

    @Override
    public Cart getById(ObjectId id) {
        return cartRepository.findById(id).orElseThrow(()->{
            log.warn("Cart or purchased no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        });
    }

    @Override
    public Cart save(Cart entity) {
        entity.getCartLines().forEach((it)->{it.setStatus(Status.PREPARADO);});
        entity.setPurchased(true);
        cartRepository.save(entity);
        return createNewCart(entity.getUserId());
    }

    public Cart createNewCart(Long userId) {
        val user=userRepository.findById(userId).get();
        val cart= Cart.builder()
                .userId(userId)
                .client(user.getClient())
                .cartLines(List.of())
                .build();
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart update(ObjectId id, Cart entity) {

    }


    @Transactional
    public String checkout(ObjectId id, Cart entity) {

        //Procesamos el stock de cada línea
        entity.getCartLines().forEach((it) -> {
            int intentos = 0;
            boolean success = false;

            while (!success) {
                try {
                    Product product = productRepository.findById(it.getProductId())
                            .orElseThrow(() -> {
                                log.warn("Product no encontrado con id: " + it.getProductId());
                                return new ProductException.NotFoundException(it.getProductId());
                            });
                    if (product.getStock() < it.getQuantity()) {
                        log.warn("Product stock negative " + it.getQuantity());
                        throw new CartException.ProductQuantityExceededException();
                    }
                    product.setStock(product.getStock() - it.getQuantity());
                    productRepository.save(product);
                    success = true;
                } catch (ObjectOptimisticLockingFailureException e) {
                    intentos++;
                    if (intentos >= 3) {
                        log.warn("demasiados intentos" + it.getProductId());
                        throw new CartException.AttemptAmountExceededException();
                    }
                }
            }
        });
        // Se crea la sesion de pago y se devuelve
        String paymentUrl = stripeService.createCheckoutSession(entity);
        return paymentUrl;
    }

    @Transactional
    public void restoreStock(ObjectId cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        // Solo se devuelve stock si NO se ha marcado como comprado todavía
        if (!cart.isPurchased()) {
            cart.getCartLines().forEach(line -> {
                productRepository.findById(line.getProductId()).ifPresent(product -> {
                    // SUMAMOS en lugar de restar
                    product.setStock(product.getStock() + line.getQuantity());
                    productRepository.save(product);
                });
            });
            log.info("Stock restaurado para el carrito: " + cartId);
        }
    }


    @Override
    @Transactional
    public void deleteById(ObjectId id) {
        var cartToEmpty = cartRepository.findById(id).orElseThrow(() -> {
            log.warn("Carrito no encontrado con id: " + id);
            return new CartException.NotFoundException("Carrito no encontrado con id: " + id);
        });
        cartToEmpty.setCartLines(List.of());
        cartRepository.save(cartToEmpty);
    }
}
