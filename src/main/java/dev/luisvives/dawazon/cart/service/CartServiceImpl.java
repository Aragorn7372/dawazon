package dev.luisvives.dawazon.cart.service;

import dev.luisvives.dawazon.cart.exceptions.CartException;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.cart.models.Status;
import dev.luisvives.dawazon.cart.repository.CartRepository;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private UserRepository userRepository;

    @Autowired
    public CartServiceImpl(ProductRepository productRepository, CartRepository cartRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }
    @Override
    public Page<Cart> findAll(Optional<Long> userId,
                              Optional<String> purchased, Pageable pageable) {
        return ;
    }

    @Override
    @Transactional
    public Cart addProduct(ObjectId id, String productId) {
        // Comprobamos que la cantidad de producto este en stock
        val productoAdd=productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Producto no encontrado con id: " + productId);
            return new ProductException.NotFoundException(productId);
        });
        val line= new CartLine().builder()
                .quantity(0)
                .productPrice(0.00)
                .productId(productId)
                .totalPrice(0.00)
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
        val productoAdd=productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Producto no encontrado con id: " + productId);
            return new ProductException.NotFoundException(productId);
        });
        val line= new CartLine().builder()
                .quantity(0)
                .productPrice(0.00)
                .productId(productId)
                .totalPrice(0.00)
                .status(Status.EN_CARRITO)
                .build();
        return cartRepository.removeCartLine(id,line).orElseThrow(()->{
            log.warn("Cart no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        });
    }

    @Override
    public Cart getById(ObjectId id) {
        return null;
    }

    @Override
    public Cart save(Cart entity) {
        return null;
    }

    @Override
    @Transactional
    public Cart update(ObjectId id, Cart entity) {
        return null;
    }

    @Override
    @Transactional
    public void deleteById(ObjectId id) {

    }
}
