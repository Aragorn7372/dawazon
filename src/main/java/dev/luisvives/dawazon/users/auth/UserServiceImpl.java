package dev.luisvives.dawazon.users.auth;

import dev.luisvives.dawazon.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {
        if (value.contains("@")) {
            return userRepository.findByEmail(value)
                    .orElseThrow(() -> new UsernameNotFoundException("Email no encontrado: " + value));
        }

        // Si no es email → es username
        return userRepository.findByUsername(value)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + value));
    }
}
