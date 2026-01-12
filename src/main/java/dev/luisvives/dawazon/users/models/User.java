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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entidad que representa un usuario del sistema.
 * <p>
 * Implementa {@link UserDetails} para integración con Spring Security.
 * Contiene información de autenticación, perfil, roles y productos favoritos.
 * </p>
 * <p>
 * <b>Campos:</b>
 * <ul>
 * <li><b>id</b> (Long): Identificador único (generado automáticamente)</li>
 * <li><b>userName</b> (String): Nombre de usuario único</li>
 * <li><b>email</b> (String): Email único</li>
 * <li><b>password</b> (String): Contraseña encriptada</li>
 * <li><b>client</b> (Client): Información del cliente embebida</li>
 * <li><b>telefono</b> (String): Teléfono del usuario</li>
 * <li><b>roles</b> (List&lt;Role&gt;): Lista de roles asignados</li>
 * <li><b>avatar</b> (String): Nombre del archivo de imagen de perfil</li>
 * <li><b>favs</b> (List&lt;String&gt;): IDs de productos favoritos</li>
 * <li><b>createdAt</b> (LocalDateTime): Fecha de creación</li>
 * <li><b>updatedAt</b> (LocalDateTime): Fecha de última actualización</li>
 * <li><b>isDeleted</b> (boolean): Marca de borrado lógico</li>
 * </ul>
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {
    /**
     * Imagen de avatar por defecto.
     */
    public final static String IMAGE_DEFAULT = "default.png";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @ElementCollection(fetch = FetchType.EAGER) // Pocos datos, tipo eager para ir más rápido
    @Enumerated(EnumType.STRING) // Guardar el nombre del enum en lugar de el "índice" del valor

    @Builder.Default
    private List<Role> roles=List.of(Role.USER);

    @Column(nullable = false)
    @Builder.Default
    private String avatar = IMAGE_DEFAULT;
    @Column()
    @ElementCollection(fetch = FetchType.EAGER) // Pocos datos, tipo eager para ir más rápido
    @Enumerated(EnumType.STRING) // Guardar el nombre del enum en lugar de el "índice" del valor
    private List<String> favs;
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

    /**
     * Obtiene las autoridades (roles) del usuario para Spring Security.
     *
     * @return Colección de autoridades con prefijo ROLE_
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el nombre de usuario para Spring Security.
     *
     * @return Nombre de usuario.
     */
    @Override
    public String getUsername() {
        return this.userName;
    }
}
