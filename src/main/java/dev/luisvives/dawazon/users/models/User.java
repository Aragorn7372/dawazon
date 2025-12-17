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
@Table(name = "users")
public class User implements UserDetails {
    public final static String IMAGE_DEFAULT = "default.png";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String userName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "client_name")),
            @AttributeOverride(name = "email", column = @Column(name = "client_email")),
            @AttributeOverride(name = "phone", column = @Column(name = "client_phone")),
            // Address anidado dentro de Client
            @AttributeOverride(name = "address. number", column = @Column(name = "client_address_number")),
            @AttributeOverride(name = "address.street", column = @Column(name = "client_address_street")),
            @AttributeOverride(name = "address.city", column = @Column(name = "client_address_city")),
            @AttributeOverride(name = "address.province", column = @Column(name = "client_address_province")),
            @AttributeOverride(name = "address. country", column = @Column(name = "client_address_country")),
            @AttributeOverride(name = "address.postalCode", column = @Column(name = "client_address_postal_code"))
    })
    private Client client;
    @Column()
    @Builder.Default
    private String telefono="";

    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER) // Pocos datos, tipo eaguer para ir mas rapido
    @Enumerated(EnumType.STRING) // Guardar el nombre del enum en lugar de el "indice" del valor Ej.: Tipo[0] = ADMIN / Tipo[1] = USER
    @Builder.Default
    private List<Role> roles=List.of(Role.USER);

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

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.userName;
    }
}
