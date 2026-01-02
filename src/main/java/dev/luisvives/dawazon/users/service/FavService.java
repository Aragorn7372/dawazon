package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz de servicio para gestión de productos favoritos de usuarios.
 */
public interface FavService {
    /**
     * Añade un producto a favoritos de un usuario.
     *
     * @param productId ID del producto
     * @param userId    ID del usuario
     */
    public void addFav(String productId, Long userId);

    /**
     * Elimina un producto de favoritos de un usuario.
     *
     * @param productId ID del producto
     * @param userId    ID del usuario
     */
    public void removeFav(String productId, Long userId);

    /**
     * Obtiene los productos favoritos de un usuario paginados.
     *
     * @param userId   ID del usuario
     * @param pageable Configuración de paginación
     * @return Página de productos favoritos
     */
    public Page<GenericProductResponseDto> getFavs(Long userId, Pageable pageable);
}
