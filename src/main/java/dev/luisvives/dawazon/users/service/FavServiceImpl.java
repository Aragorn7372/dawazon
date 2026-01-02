package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import dev.luisvives.dawazon.products.mapper.ProductMapper;
import dev.luisvives.dawazon.products.repository.ProductRepository;
import dev.luisvives.dawazon.users.exceptions.UserException;
import dev.luisvives.dawazon.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Implementación del servicio de gestión de productos favoritos de usuarios.
 * <p>
 * Permite añadir, eliminar y consultar productos favoritos de un usuario.
 * </p>
 */
@Service
@Slf4j
public class FavServiceImpl implements FavService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param userRepository    Repositorio de usuarios
     * @param productRepository Repositorio de productos
     * @param productMapper     Mapper de productos
     */
    @Autowired
    public FavServiceImpl(UserRepository userRepository, ProductRepository productRepository,
            ProductMapper productMapper) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    /**
     * Añade un producto a los favoritos de un usuario.
     * <p>
     * Verifica que el usuario existe y que el producto no esté ya en favoritos.
     * </p>
     *
     * @param productId ID del producto
     * @param userId    ID del usuario
     * @throws UserException.UserNotFoundException          Si el usuario no existe
     * @throws UserException.UserHasThatFavProductException Si el producto ya está
     *                                                      en favoritos
     */
    @Override
    public void addFav(String productId, Long userId) {
        val user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNotFoundException("no encontrado usuario con id" + userId));
        if (user.getFavs().contains(productId)) {
            throw new UserException.UserHasThatFavProductException("ya tienes en favoritos a ese producto");
        }
        user.getFavs().add(productId);
        userRepository.save(user);
    }

    /**
     * Elimina un producto de los favoritos de un usuario.
     * <p>
     * Verifica que el usuario existe y que el producto esté en favoritos antes de
     * eliminarlo.
     * </p>
     *
     * @param productId ID del producto
     * @param userId    ID del usuario
     * @throws UserException.UserNotFoundException          Si el usuario no existe
     * @throws UserException.UserHasThatFavProductException Si el producto no estaba
     *                                                      en favoritos
     */
    @Override
    public void removeFav(String productId, Long userId) {
        val user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNotFoundException("no encontrado usuario con id" + userId));
        if (!user.getFavs().contains(productId)) {
            throw new UserException.UserHasThatFavProductException("no tenias en favoritos a ese producto");
        }
        user.getFavs().remove(productId);
        userRepository.save(user);
    }

    /**
     * Obtiene los productos favoritos de un usuario de forma paginada.
     * <p>
     * Recupera todos los productos favoritos del usuario, filtra los que no
     * existen,
     * y los convierte a DTOs con sus comentarios completos.
     * </p>
     *
     * @param userId   ID del usuario
     * @param pageable Configuración de paginación
     * @return Página de productos favoritos como DTOs
     * @throws UserException.UserNotFoundException Si el usuario no existe
     */
    @Override
    public Page<GenericProductResponseDto> getFavs(Long userId, Pageable pageable) {
        val user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNotFoundException("no encontrado usuario con id" + userId));
        val products = user.getFavs().stream().map(it -> productRepository.findById(it).orElse(null));

        return new PageImpl<>(products.toList().stream().filter(Objects::nonNull)
                .map((it) -> productMapper.modelToGenericResponseDTO(
                        it, it.getComments().stream()
                                .map((comment) -> productMapper.commentToCommentDto(comment,
                                        userRepository.findActiveById(comment.getUserId()).getUsername()))
                                .toList()))
                .toList(),
                pageable, user.getFavs().size());
    }
}
