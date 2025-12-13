package dev.luisvives.dawazon.users.models;

import dev.luisvives.dawazon.cart.models.Client;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User implements UserDetails {
    final String IMAGE_DEFAULT = "default.png";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String userName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private Client client;

    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER) // Pocos datos, tipo eaguer para ir mas rapido
    @Enumerated(EnumType.STRING) // Guardar el nombre del enum en lugar de el "indice" del valor Ej.: Tipo[0] = ADMIN / Tipo[1] = USER
    private List<Role> roles;

    @Column(nullable = false)
    @Builder.Default
    private String avatar = IMAGE_DEFAULT;

    @Column(nullable = false)
    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @LastModifiedDate
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.userName;
    }
}
