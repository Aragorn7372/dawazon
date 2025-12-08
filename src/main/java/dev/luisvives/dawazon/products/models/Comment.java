package dev.luisvives.dawazon.products.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Comment {
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private String content;
    @Column(nullable = false)
    private boolean verified;
    @Column(nullable = false)
    private boolean recommended;
    @Column(nullable = false)
    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
