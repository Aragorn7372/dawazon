package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserService {
    @Autowired
    UserRepository repositorio;
        @Override
        public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {
            if (value.contains("@")) {
                return repositorio.findByEmail(value)
                        .orElseThrow(() -> new UsernameNotFoundException("Email no encontrado: " + value));
            }

            // Si no es email → es username
            return repositorio.findByUserName(value)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + value));
        }
}

