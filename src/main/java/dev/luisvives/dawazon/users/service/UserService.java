package dev.luisvives.dawazon.users.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Interfaz de servicio de usuarios que extiende UserDetailsService de Spring
 * Security.
 * <p>
 * Permite cargar usuarios por nombre de usuario o email para autenticación.
 * </p>
 */
public interface UserService extends UserDetailsService {
    /**
     * Carga un usuario por su nombre de usuario o email.
     *
     * @param value Nombre de usuario o email
     * @return Detalles del usuario
     * @throws UsernameNotFoundException Si no se encuentra el usuario
     */
    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException;
}
