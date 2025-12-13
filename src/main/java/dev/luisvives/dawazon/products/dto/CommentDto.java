package dev.luisvives.dawazon.products.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private String userName;
    private String comment;
    private boolean recommended;
    private boolean verified;
}
