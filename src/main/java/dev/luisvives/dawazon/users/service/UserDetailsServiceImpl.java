package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio UserDetailsService para Spring Security.
 * <p>
 * Carga usuarios por nombre de usuario o email desde la base de datos.
 * </p>
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserService {
    /**
     * Repositorio de usuarios.
     */
    @Autowired
    UserRepository repositorio;

    /**
     * Carga un usuario por su nombre de usuario o email.
     * <p>
     * Detecta automáticamente si el valor es un email (contiene @) o un nombre de
     * usuario.
     * </p>
     *
     * @param value Nombre de usuario o email
     * @return Detalles del usuario
     * @throws UsernameNotFoundException Si no se encuentra el usuario
     */
    @Override
    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {
        if (value.contains("@")) {
            return repositorio.findByEmailAndIsDeletedFalse(value)
                    .orElseThrow(() -> new UsernameNotFoundException("Email no encontrado: " + value));
        }

        // Si no es email → es username
        return repositorio.findByUserNameAndIsDeletedFalse(value)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + value));
    }
}
