package dev.luisvives.dawazon.products.mapper;

import dev.luisvives.dawazon.common.dto.PageResponseDTO;
import dev.luisvives.dawazon.products.dto.CommentDto;
import dev.luisvives.dawazon.products.dto.GenericProductResponseDto;
import dev.luisvives.dawazon.products.dto.PostProductRequestDto;
import dev.luisvives.dawazon.products.models.Comment;
import dev.luisvives.dawazon.products.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {
    public GenericProductResponseDto modelToGenericResponseDTO(Product productoFound, List<CommentDto> commentsFound) {
        return GenericProductResponseDto.builder()
                .id(productoFound.getId())
                .name(productoFound.getName())
                .price(productoFound.getPrice())
                .category(productoFound.getCategory().getName())
                .image(productoFound.getImages().stream().map(it->{return "/files/"+it;}).toList())
                .stock(productoFound.getStock())
                .comments(commentsFound)
                .description(productoFound.getDescription()).build();
    }

    public Product postPutDTOToModel(PostProductRequestDto productoDto) {
        return Product.builder()
                .id(productoDto.getId())
                .name(productoDto.getName())
                .price(productoDto.getPrice())
                .stock(productoDto.getStock())
                .description(productoDto.getDescription())
                //.category(productoDto.getCategory()) Esto en el servicio
                .creatorId(productoDto.getCreatorId())
                .build();
    }

    public CommentDto commentToCommentDto(Comment comment,String userName){
        return CommentDto.builder()
                .comment(comment.getContent())
                .userName(userName)
                .verified(comment.isVerified())
                .recommended(comment.isRecommended())
                .build();
    }

    public PageResponseDTO<Product> pageToDTO (Page<Product> page, String sortBy, String direction) {
        return new PageResponseDTO<>(
                page.getContent()
                        .stream()
                        .map(it -> it)
                        .toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber(),
                page.getNumberOfElements(),
                page.isEmpty(),
                page.isFirst(),
                page.isLast(),
                sortBy,
                direction
        );
    }
}
