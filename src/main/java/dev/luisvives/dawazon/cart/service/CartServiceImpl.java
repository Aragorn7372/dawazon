package dev.luisvives.dawazon.cart.service;

import dev.luisvives.dawazon.cart.dto.CartStockRequestDto;
import dev.luisvives.dawazon.cart.dto.LineRequestDto;
import dev.luisvives.dawazon.cart.dto.SaleLineDto;
import dev.luisvives.dawazon.cart.exceptions.CartException;
import dev.luisvives.dawazon.cart.mapper.CartMapper;
import dev.luisvives.dawazon.cart.models.Cart;
import dev.luisvives.dawazon.cart.models.CartLine;
import dev.luisvives.dawazon.cart.models.Status;
import dev.luisvives.dawazon.cart.repository.CartRepository;
import dev.luisvives.dawazon.common.email.OrderEmailService;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de carritos.
 * <p>
 * Maneja la lógica completa del carrito: añadir/eliminar productos,
 * checkout con Stripe, gestión de stock y procesamiento de ventas.
 * </p>
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {
    /**
     * Repositorio de productos.
     */
    private final ProductRepository productRepository;

    /**
     * Repositorio de carritos.
     */
    private final CartRepository cartRepository;

    /**
     * Servicio de Stripe para pagos.
     */
    private final StripeService stripeService;

    /**
     * Repositorio de usuarios.
     */
    private final UserRepository userRepository;

    /**
     * Template de MongoDB.
     */
    private final MongoTemplate mongoTemplate;

    /**
     * Mapper de carritos.
     */
    private final CartMapper mapper;

    /**
     * Servicio de emails.
     */
    private final OrderEmailService mailService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param productRepository Repositorio de productos
     * @param cartRepository    Repositorio de carritos
     * @param userRepository    Repositorio de usuarios
     * @param stripeService     Servicio de Stripe
     * @param mongoTemplate     Template de MongoDB
     * @param cartMapper        Mapper de carritos
     * @param mailservice       Servicio de emails
     */
    @Autowired
    public CartServiceImpl(ProductRepository productRepository, CartRepository cartRepository,
            UserRepository userRepository, StripeService stripeService, MongoTemplate mongoTemplate,
            CartMapper cartMapper, OrderEmailService mailservice) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.stripeService = stripeService;
        this.mongoTemplate = mongoTemplate;
        this.mapper = cartMapper;
        this.mailService = mailservice;
    }

    /**
     * Obtiene todas las ventas como líneas individuales para administración.
     * <p>
     * Filtra por manager y permisos de admin. Pagina manualmente los resultados.
     * </p>
     *
     * @param managerId ID del vendedor (opcional)
     * @param isAdmin   Si el usuario es administrador
     * @param pageable  Parámetros de paginación
     * @return Página de líneas de venta
     */
    public Page<SaleLineDto> findAllSalesAsLines(
            Optional<Long> managerId,
            boolean isAdmin,
            Pageable pageable) {

        log.info("Buscando ventas - Manager: {},  isAdmin: {}",
                managerId.orElse(null), isAdmin);

        // Obtenemos los carritos comprados
        Query query = new Query();
        query.addCriteria(Criteria.where("purchased").is(true));

        List<Cart> purchasedCarts = mongoTemplate.find(query, Cart.class);
        log.info("Carritos comprados encontrados: {}", purchasedCarts.size());

        // Convertimos cada CartLine a SaleLineDto, añadiendo datos extra para la vista
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
        filteredLines.sort(Comparator.comparing(SaleLineDto::getCreatedAt).reversed());
        // Obtenemos el desfase y lo usamos como primer elemento
        int start = (int) pageable.getOffset();
        // Obtenemos el numero del ultimo elemento de la pagina
        int end = Math.min(start + pageable.getPageSize(), filteredLines.size());
        // creamos la lista y verificamos que exista es decir que si falla el filtro ya
        // que en el indice
        // inicial y en el índice final no hay lineas de pedido devuelve una lista vacia
        List<SaleLineDto> paginatedLines = start < filteredLines.size()
                ? filteredLines.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(paginatedLines, pageable, filteredLines.size());
    }

    /**
     * Calcula las ganancias totales de ventas.
     *
     * @param managerId ID del vendedor (opcional)
     * @param isAdmin   Si el usuario es administrador
     * @return Total de ganancias
     */
    public Double calculateTotalEarnings(Optional<Long> managerId, boolean isAdmin) {
        Query query = new Query();
        query.addCriteria(Criteria.where("purchased").is(true));

        List<Cart> purchasedCarts = mongoTemplate.find(query, Cart.class);

        return purchasedCarts.stream()
                .flatMap(cart -> cart.getCartLines().stream())
                .filter(line -> {
                    if (isAdmin && managerId.isEmpty())
                        return true;
                    if (managerId.isEmpty())
                        return false;

                    Product product = productRepository.findById(line.getProductId()).orElse(null);
                    return product != null && product.getCreatorId().equals(managerId.get());
                })
                .mapToDouble(CartLine::getTotalPrice)
                .sum();
    }

    /**
     * Busca carritos con filtros opcionales.
     *
     * @param userId    Filtro opcional por usuario
     * @param purchased Filtro opcional por estado de compra
     * @param pageable  Parámetros de paginación
     * @return Página de carritos
     */
    @Override
    public Page<Cart> findAll(Optional<Long> userId,
            Optional<String> purchased,
            Pageable pageable) {

        // creamos query de mongo
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        // Filtro por User ID (si está presente)
        userId.ifPresent(id -> criteriaList.add(Criteria.where("userId").is(id)));
        // Filtro por Estado de Compra (purchased)
        // El input es String ("true"/"false"), pero en BD es boolean. Hacemos el
        // parseo.
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

    /**
     * Añade un producto al carrito.
     *
     * @param id        ID del carrito
     * @param productId ID del producto a añadir
     * @return Carrito actualizado
     * @throws ProductException.NotFoundException Si el producto no existe
     * @throws CartException.NotFoundException    Si el carrito no existe
     */
    @Override
    @Transactional
    public Cart addProduct(ObjectId id, String productId) {
        log.info("Añadiendo producto " + productId + " a " + id);
        // Comprobamos que la cantidad de producto esté en stock
        val product = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Producto no encontrado con id: " + productId);
            return new ProductException.NotFoundException(productId);
        });
        val line = CartLine.builder()
                .quantity(1)
                .productPrice(product.getPrice())
                .productId(productId)
                .status(Status.EN_CARRITO)
                .totalPrice(1 * product.getPrice())
                .build();
        cartRepository.addCartLine(id, line);

        // Recuperar carrito y recalcular totales
        Cart cart = cartRepository.findById(id).orElseThrow(() -> {
            log.warn("Cart no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        });

        // Recalcular totalItems y total
        cart.setTotalItems(cart.getCartLines().size());
        cart.setTotal(cart.getCartLines().stream()
                .mapToDouble(CartLine::getTotalPrice)
                .sum());

        // Guardar el carrito con los totales actualizados
        return cartRepository.save(cart);
    }

    /**
     * Obtiene una línea de venta específica por IDs.
     *
     * @param cartId    ID del carrito
     * @param productId ID del producto
     * @param managerId ID del vendedor
     * @param isAdmin   Si el usuario es administrador
     * @return DTO de línea de venta
     * @throws CartException.NotFoundException     Si no se encuentra
     * @throws CartException.UnauthorizedException Si no tiene permisos
     */
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

        User manager = userRepository.findById(product.getCreatorId()).orElseThrow(() -> {
            log.warn("User no encontrado con id: " + product.getCreatorId());
            return new UserException.UserNotFoundException("User no encontrado con id: " + product.getCreatorId());
        });

        return mapper.cartlineToSaleLineDto(cart, product, line, manager);
    }

    /**
     * Elimina un producto del carrito.
     *
     * @param id        ID del carrito
     * @param productId ID del producto a eliminar
     * @return Carrito actualizado
     * @throws CartException.NotFoundException Si el carrito no existe
     */
    @Override
    @Transactional
    public Cart removeProduct(ObjectId id, String productId) {
        log.info("Eliminando producto del carrito, con ID: " + productId);
        val cartLine = cartRepository.findById(id).orElseThrow(() -> {
            log.warn("Cart no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        }).getCartLines().stream().filter((it) -> it.getProductId().equals(productId)).findFirst().get();
        val line = CartLine.builder()
                .quantity(cartLine.getQuantity())
                .productPrice(cartLine.getProductPrice())
                .productId(productId)
                .status(Status.EN_CARRITO)
                .totalPrice(cartLine.getQuantity() * cartLine.getProductPrice())
                .build();
        cartRepository.removeCartLine(id, line);

        // Recuperar carrito y recalcular totales
        Cart cart = cartRepository.findById(id).orElseThrow(() -> {
            log.warn("Cart no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        });

        // Recalcular totalItems y total
        cart.setTotalItems(cart.getCartLines().size());
        cart.setTotal(cart.getCartLines().stream()
                .mapToDouble(CartLine::getTotalPrice)
                .sum());

        // Guardar el carrito con los totales actualizados
        return cartRepository.save(cart);
    }

    /**
     * Obtiene un carrito por su ID.
     *
     * @param id ID del carrito
     * @return Carrito encontrado
     * @throws CartException.NotFoundException Si el carrito no existe
     */
    @Override
    public Cart getById(ObjectId id) {
        return cartRepository.findById(id).orElseThrow(() -> {
            log.warn("Cart no encontrado con id: " + id);
            return new CartException.NotFoundException("Cart no encontrado con id: " + id);
        });
    }

    /**
     * Guarda un carrito como pedido completado y crea uno nuevo para el usuario.
     *
     * @param entity Carrito a guardar como pedido
     * @return Carrito guardado con purchased=true
     */
    @Override
    public Cart save(Cart entity) {
        entity.getCartLines().forEach((it) -> {
            it.setStatus(Status.PREPARADO);
        });
        entity.setPurchased(true);
        entity.setCheckoutInProgress(false);
        entity.setCheckoutStartedAt(null);
        val savedCart = cartRepository.save(entity);
        createNewCart(entity.getUserId());
        return savedCart;
    }

    /**
     * Envía email de confirmación en un hilo separado
     * 
     * @param pedido El {@link Cart} para el cual se enviará el email de
     *               confirmación.
     */
    public void sendConfirmationEmailAsync(Cart pedido) {
        Thread emailThread = new Thread(() -> {
            try {
                log.info("Iniciando envío de email en hilo separado para pedido: {}", pedido.getId());

                // Enviar el email (irá a Mailtrap en desarrollo)
                mailService.enviarConfirmacionPedidoHtml(pedido);

                log.info("Email de confirmación enviado correctamente para pedido: {}", pedido.getId());

            } catch (Exception e) {
                log.warn("Error enviando email de confirmación para pedido {}: {}",
                        pedido.getId(), e.getMessage());

                // El error no se propaga - el pedido ya está guardado
            }
        });

        // Configurar el hilo
        emailThread.setName("EmailSender-Pedido-" + pedido.getId());
        emailThread.setDaemon(true); // No impide que la aplicación se cierre

        // Iniciar el hilo (no bloqueante)
        emailThread.start();

        log.info("Hilo de email iniciado para pedido: {}", pedido.getId());
    }

    /**
     * Actualiza la cantidad de stock de un producto en el carrito.
     *
     * @param entity DTO con datos de actualización de stock
     * @return Carrito actualizado
     * @throws CartException.NotFoundException Si el carrito no existe
     */
    @Override
    public Cart updateStock(CartStockRequestDto entity) {
        val cart = cartRepository.findById(entity.getCartId()).orElseThrow(
                () -> new CartException.NotFoundException("Cart no encontrado con id: " + entity.getCartId()));
        cart.getCartLines().stream().filter((it) -> it.getProductId().equals(entity.getProductId())).findFirst().get()
                .setQuantity(entity.getQuantity());
        cartRepository.save(cart);
        return cart;
    }

    /**
     * Actualiza la cantidad de un producto en el carrito con validaciones.
     * <p>
     * Valida que la cantidad sea al menos 1 y que haya stock suficiente.
     * Recalcula los totales del carrito automáticamente.
     * </p>
     *
     * @param entity DTO con datos de actualización de stock
     * @return Carrito actualizado
     * @throws CartException.NotFoundException          Si el carrito no existe
     * @throws CartException.InsufficientStockException Si no hay stock suficiente
     * @throws IllegalArgumentException                 Si la cantidad es menor que
     *                                                  1
     */
    @Transactional
    public Cart updateStockWithValidation(CartStockRequestDto entity) {
        // Buscamos el carrito
        Cart cart = cartRepository.findById(entity.getCartId())
                .orElseThrow(() -> new CartException.NotFoundException(
                        "Cart no encontrado con id: " + entity.getCartId()));

        // Validamos la cantidad mínima
        if (entity.getQuantity() < 1) {
            throw new IllegalArgumentException("La cantidad mínima es 1");
        }

        // Buscamos el producto para verificar stock
        Product product = productRepository.findById(entity.getProductId())
                .orElseThrow(() -> new ProductException.NotFoundException(entity.getProductId()));

        // Verificamos el stock disponible
        if (product.getStock() < entity.getQuantity()) {
            throw new CartException.InsufficientStockException(
                    "Stock insuficiente. Solo hay " + product.getStock() + " unidades disponibles.");
        }

        // Buscamos y actualizamos la línea del carrito
        cart.getCartLines().stream()
                .filter(line -> line.getProductId().equals(entity.getProductId()))
                .findFirst()
                .ifPresent(line -> {
                    line.setQuantity(entity.getQuantity());
                    // setQuantity ya calcula totalPrice automáticamente
                });

        // Recalculamos los totales del carrito
        cart.setTotalItems(cart.getCartLines().size());
        cart.setTotal(cart.getCartLines().stream()
                .mapToDouble(CartLine::getTotalPrice)
                .sum());

        // Guardamos el carrito
        return cartRepository.save(cart);
    }

    /**
     * Crea un nuevo carrito vacío para un usuario.
     *
     * @param userId ID del usuario
     * @return Nuevo carrito creado
     */
    public Cart createNewCart(Long userId) {
        val user = userRepository.findById(userId).get();
        val cart = Cart.builder()
                .userId(userId)
                .client(user.getClient())
                .cartLines(List.of())
                .build();
        return cartRepository.save(cart);
    }

    /**
     * Actualiza el estado de una línea de carrito.
     *
     * @param line DTO con datos de la línea a actualizar
     * @return Carrito actualizado
     * @throws CartException.NotFoundException Si el carrito no existe
     */
    @Transactional
    public Cart update(LineRequestDto line) {
        cartRepository.updateCartLineStatus(line.getCartId(), line.getProductId(), line.getStatus());
        return cartRepository.findById(line.getCartId()).orElseThrow(() -> {
            log.warn("Cart no encontrado con id: " + line.getCartId());
            return new CartException.NotFoundException("Cart no encontrado con id: " + line.getCartId());
        });
    }

    /**
     * Procesa el checkout del carrito.
     * <p>
     * Marca el checkout como en progreso, descuenta el stock con reintentos
     * en caso de conflictos de concurrencia, y crea la sesión de pago en Stripe.
     * </p>
     *
     * @param id     ID del carrito
     * @param entity Carrito a procesar
     * @return URL de pago de Stripe
     * @throws ProductException.NotFoundException             Si un producto no
     *                                                        existe
     * @throws CartException.ProductQuantityExceededException Si no hay stock
     *                                                        suficiente
     * @throws CartException.AttemptAmountExceededException   Si se superan los
     *                                                        reintentos
     */
    @Transactional
    public String checkout(ObjectId id, Cart entity) {
        entity.setCheckoutInProgress(true);
        entity.setCheckoutStartedAt(LocalDateTime.now());
        val user=userRepository.findById(entity.getUserId()).orElseThrow(()->{
            log.warn("User no encontrado con id: " + entity.getUserId());
            return new UserException.UserNotFoundException("User no encontrado con id: " + entity.getUserId()) {
            };
        });
        val cart=entity;
        cart.setClient(user.getClient());
        cartRepository.save(entity);
        // Procesamos el stock de cada línea
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
                        log.warn("Producto stock negativo " + it.getQuantity());
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

    /**
     * Restaura el stock de productos de un carrito no comprado.
     * <p>
     * Solo restaura si el carrito NO ha sido marcado como comprado.
     * </p>
     *
     * @param cartId ID del carrito
     */
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
            cart.setCheckoutInProgress(false);
            cart.setCheckoutStartedAt(null);
            cartRepository.save(cart);
            log.info("Stock restaurado para el carrito: " + cartId);
        }
    }

    /**
     * Vacía un carrito eliminando todas sus líneas.
     *
     * @param id ID del carrito a vaciar
     * @throws CartException.NotFoundException Si el carrito no existe
     */
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

    /**
     * Obtiene múltiples productos por sus IDs.
     *
     * @param productsIds Lista de IDs de productos
     * @return Lista de productos encontrados
     * @throws ProductException.NotFoundException Si algún producto no existe
     */
    @Override
    public List<Product> variosPorId(List<String> productsIds) {

        List<Product> products = new ArrayList<>();

        productsIds.forEach(id -> {
            val product = productRepository.findById(id)
                    .orElseThrow(
                            () -> new ProductException.NotFoundException("No se encontró el producto con id: " + id) {
                            });
            products.add(product);
        });

        return products;
    }

    /**
     * Obtiene el carrito activo (no comprado) de un usuario.
     *
     * @param userId ID del usuario
     * @return Carrito activo del usuario
     * @throws CartException.NotFoundException Si no existe carrito activo
     */
    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserIdAndPurchased(userId, false)
                .orElseThrow(() -> {
                    log.error("Carrito no encontrado para userId: " + userId);
                    return new CartException.NotFoundException("Carrito no encontrado para userId: " + userId);
                });
    }

    /**
     * Cancela una venta específica y restaura el stock.
     * <p>
     * Verifica permisos y solo cancela si no estaba ya cancelada.
     * </p>
     *
     * @param ventaId   ID del carrito/venta
     * @param productId ID del producto
     * @param managerId ID del vendedor actual
     * @param isAdmin   Si el usuario es administrador
     * @throws CartException.NotFoundException     Si no se encuentra
     * @throws CartException.UnauthorizedException Si no tiene permisos
     */
    @Override
    @Transactional
    public void cancelSale(String ventaId, String productId, Long managerId, boolean isAdmin) {

        ObjectId cartObjectId = new ObjectId(ventaId);
        Cart cart = cartRepository.findById(cartObjectId)
                .orElseThrow(() -> new CartException.NotFoundException("Venta no encontrada"));

        CartLine line = cart.getCartLines().stream()
                .filter(l -> l.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartException.NotFoundException("Producto no encontrado en esta venta"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException.NotFoundException(productId));

        if (!isAdmin && !product.getCreatorId().equals(managerId)) {
            throw new CartException.UnauthorizedException("No tienes permisos para cancelar esta venta");
        }

        if (line.getStatus() != Status.CANCELADO) {
            line.setStatus(Status.CANCELADO);

            product.setStock(product.getStock() + line.getQuantity());
            productRepository.save(product);

            cartRepository.save(cart);
            log.info("Venta cancelada: Cart {} Product {}", ventaId, productId);
        }
    }

    /**
     * Limpia carritos con checkout expirado (más de 5 minutos).
     * <p>
     * Tarea programada que restaura el stock de carritos abandonados
     * durante el proceso de pago.
     * </p>
     *
     * @return Número de carritos limpiados exitosamente
     */
    @Transactional
    public int cleanupExpiredCheckouts() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(5);

        Query query = new Query();
        query.addCriteria(Criteria.where("checkoutInProgress").is(true)
                .and("purchased").is(false)
                .and("checkoutStartedAt").lt(expirationTime));

        List<Cart> expiredCarts = mongoTemplate.find(query, Cart.class);

        if (expiredCarts.isEmpty()) {
            log.debug("No hay carritos expirados para limpiar");
            return 0;
        }

        log.info("Limpiando {} carritos expirados", expiredCarts.size());

        int cleanedCount = 0;

        for (Cart cart : expiredCarts) {
            try {
                log.warn("Carrito {} expirado ({} minutos). Restaurando stock...",
                        cart.getId(), cart.getMinutesSinceCheckoutStarted());

                // Restaurar stock
                restoreStock(new ObjectId(cart.getId()));

                cleanedCount++;

                log.info("Carrito {} limpiado exitosamente", cart.getId());

            } catch (Exception e) {
                log.error("Error restaurando stock del carrito {}: {}",
                        cart.getId(), e.getMessage(), e);
            }
        }

        log.info("Limpieza completada:  {}/{} carritos procesados",
                cleanedCount, expiredCarts.size());

        return cleanedCount;
    }
}