package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavService {
    public void addFav(String productId, Long userId);
    public void removeFav(String productId, Long userId);
    public Page<GenericProductResponseDto> getFavs(Long userId, Pageable pageable);
}
