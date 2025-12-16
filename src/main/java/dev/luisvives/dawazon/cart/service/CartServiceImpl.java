package dev.luisvives.dawazon.cart.service;

import dev.luisvives.dawazon.cart.dto.LineRequestDto;
import dev.luisvives.dawazon.cart.dto.SaleLineDto;
import dev.luisvives.dawazon.cart.exceptions.CartException;
import dev.luisvives.dawazon.cart.mapper.CartMapper;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.cart.models.Status;
import dev.luisvives.dawazon.cart.repository.CartRepository;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.products.models.Product;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.stripe.service.StripeService;
import dev.luisvives.dawazon.users.exceptions.UserException;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final StripeService stripeService;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final CartMapper mapper;

    @Autowired
    public CartServiceImpl(ProductRepository productRepository, CartRepository cartRepository, UserRepository userRepository, StripeService stripeService, MongoTemplate mongoTemplate, CartMapper cartMapper) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.stripeService = stripeService;
        this.mongoTemplate = mongoTemplate;
        this.mapper = cartMapper;
    }

    public Page<SaleLineDto> findAllSalesAsLines(
            Optional<Long> managerId,
            boolean isAdmin,
            Pageable pageable) {

        log.info("Buscando ventas - Manager: {},  isAdmin: {}",
                managerId.orElse(null),  isAdmin);

        // Obtenemos los carritos comprados
        Query query = new Query();
        query.addCriteria(Criteria.where("purchased").is(true));

        List<Cart> purchasedCarts = mongoTemplate.find(query, Cart.class);
        log.info("Carritos comprados encontrados: {}", purchasedCarts.size());

        // Convertimos cada CartLine a SaleLineDto, añadiendo datos extra para la vista de verga
        List<SaleLineDto> allSaleLines = new ArrayList<>();

        for (Cart cart : purchasedCarts) {
            for (CartLine line : cart.getCartLines()) {
                try {
                    // obtenemos los productos
                    Product product = productRepository.findById(line.getProductId()).get();

                    // Obtener información del manager
                    User manager = userRepository.findById(product.getCreatorId()).get();

                    // Crear el DTO
                    SaleLineDto saleLineDto = mapper.cartlineToSaleLineDto(cart, product, line, manager);

                    allSaleLines.add(saleLineDto);

                } catch (Exception e) {
                    log.error("Error procesando línea de venta: {}", e.getMessage());
                }
            }
        }

        // Filtramos según permisos y parámetros
        List<SaleLineDto> filteredLines = allSaleLines.stream()
                .filter(sale -> {
                    // Si no es admin, solo ve sus propias ventas
                    if (!isAdmin && managerId.isPresent()) {
                        return sale.getManagerId().equals(managerId.get());
                    }
                    return true;
                }).collect(Collectors.toList());

        log.info("Ventas filtradas: {}", filteredLines.size());

        // Ordenamos y paginamos manualmente
        filteredLines.sort(Comparator.comparing(SaleLineDto:: getCreatedAt).reversed());
        // Obtenemos el desfase y lo usamos como primer elemento
        int start = (int) pageable.getOffset();
        // Obtenemos el numero del ultimo elemento de la pagina
        int end = Math.min(start + pageable.getPageSize(), filteredLines.size());
        // creamos la lista y verificamos que exista es decir que si falla el filtro ya que en el indice
        // inicial y en el indice final no hay lineas de pedido devuelve una lista vacia
        List<SaleLineDto> paginatedLines = start < filteredLines.size()
                ? filteredLines.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(paginatedLines, pageable, filteredLines.size());
    }
    public Double calculateTotalEarnings(Optional<Long> managerId, boolean isAdmin) {
        Query query = new Query();
        query.addCriteria(Criteria.where("purchased").is(true));

        List<Cart> purchasedCarts = mongoTemplate.find(query, Cart.class);

        return purchasedCarts.stream()
                .flatMap(cart -> cart.getCartLines().stream())
                .filter(line -> {
                    if (isAdmin && managerId.isEmpty()) return true;
                    if (managerId.isEmpty()) return false;

                    Product product = productRepository.findById(line.getProductId()).orElse(null);
                    return product != null && product.getCreatorId().equals(managerId.get());
                })
                .mapToDouble(CartLine::getTotalPrice)
                .sum();
    }
    @Override
    public Page<Cart> findAll(Optional<Long> userId,
                              Optional<String> purchased,
                              Pageable pageable) {

            // creamos query de mongo
            Query query = new Query();
            List<Criteria> criteriaList = new ArrayList<>();
            // Filtro por User ID (si está presente)
            userId.ifPresent(id ->
                    criteriaList.add(Criteria.where("userId").is(id))
            );
            // Filtro por Estado de Compra (purchased)
            // El input es String ("true"/"false"), pero en BD es boolean. Hacemos el parseo.
            purchased.ifPresent(p -> {
                boolean isPurchased = Boolean.parseBoolean(p);
                criteriaList.add(Criteria.where("purchased").is(isPurchased));
            });
            // Aplicar los criterios
            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }
            // Contar el total de elementos (necesario para el objeto Page)
            // Esto se hace ANTES de aplicar la paginación a la query
            long count = mongoTemplate.count(query, Cart.class);
            // Aplicar paginación y ordenación
            query.with(pageable);
            // Ejecutar la búsqueda
            List<Cart> carts = mongoTemplate.find(query, Cart.class);
            // Retornar la página
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
    public SaleLineDto getSaleLineByIds(String cartId, String productId, Long managerId, boolean isAdmin) {
        ObjectId objectId = new ObjectId(cartId);
        Cart cart = cartRepository.findById(objectId)
                .orElseThrow(() -> new CartException.NotFoundException("Carrito no encontrado"));

        CartLine line = cart.getCartLines().stream()
                .filter(l -> l.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartException.NotFoundException("Línea de venta no encontrada"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException.NotFoundException(productId));

        // Verificar permisos
        if (!isAdmin && !product.getCreatorId().equals(managerId)) {
            throw new CartException.UnauthorizedException("No tienes permisos para ver esta venta");
        }

        User manager = userRepository.findById(product.getCreatorId()).orElseThrow(()->{
            log.warn("User no encontrado con id: " + product.getCreatorId());
            return new UserException.UserNotFoundException("User no encontrado con id: " + product.getCreatorId());
        });

        return mapper.cartlineToSaleLineDto(cart,product,line,manager);
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

    @Transactional
    public Cart update(LineRequestDto line) {
        return cartRepository.updateCartLineStatus(line.getCartId(), line.getProductId(), line.getStatus()).orElseThrow(() -> {
            log.warn("Cart no encontrado con id: " + line.getCartId());
            return new CartException.NotFoundException("Cart no encontrado con id: " + line.getCartId());
        });
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

    @Override
    public List<Product> variosPorId(List<String> productsIds){

        List<Product> products = new ArrayList<>();

        productsIds.forEach(id -> {
            val product = productRepository.findById(id)
                    .orElseThrow(() -> new ProductException.NotFoundException("No se encontró el producto con id: " +id) {
                    });
            products.add(product);
        });

        return products;
    }
}
