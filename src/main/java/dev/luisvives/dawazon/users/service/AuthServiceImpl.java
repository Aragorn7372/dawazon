package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.common.storage.service.FileSystemStorageService;
import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.users.dto.UserChangePasswordDto;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.exceptions.UserException;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static dev.luisvives.dawazon.users.models.User.IMAGE_DEFAULT;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final StorageService storage;
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository repositorio,
                           BCryptPasswordEncoder passwordEncoder,
                           UserRepository userRepository,
                           FileSystemStorageService storage) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.storage = storage;
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    @Override
    public User register(User u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return userRepository.save(u);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    @Override
    public User changePassword(UserChangePasswordDto userDto, Long id) {
        val user=userRepository.findById(id).orElseThrow(()->new UsernameNotFoundException("User not found"));
        if(passwordEncoder.matches(userDto.getOldPassword(), user.getPassword()) && passwordEncoder.matches(userDto.getNewPassword(), user.getPassword()) && userDto.getNewPassword().equals(userDto.getConfirmPassword())){
                    user.setPassword(passwordEncoder.encode(userDto.getNewPassword()));
                    return userRepository.save(user);
        }else {
            throw new UserException.UserPasswordNotMatchException("contraseña incorrecta");
        }
    }

    @Override
    public User updateCurrentUser(Long id, UserRequestDto updateUser) {
        var user=userRepository.findById(id).orElseThrow(()->new UsernameNotFoundException("User not found"));
        user.setUserName(updateUser.getNombre());
        user.setEmail(updateUser.getEmail());
        user.setAvatar(updateUser.getAvatar());
        user.setTelefono(updateUser.getTelefono());
        return userRepository.save(user);
    }

    @Override
    public User updateImage(Long id, MultipartFile image) {
        val user = userRepository.findById(id)
                .orElseThrow(() -> new ProductException.NotFoundException("Producto no encontrado con id: " + id));
        log.info("Actualizando imagen de producto por id: " + id);

        if (user.getAvatar() != IMAGE_DEFAULT) {
            storage.delete(user.getAvatar());
        }

        String imageStored = storage.store(image);

        User userUpdated = User.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .telefono(user.getTelefono())
                .client(user.getClient())
                .password(user.getPassword())
                .roles(user.getRoles())
                .avatar(imageStored)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return userRepository.save(userUpdated);


    }

    @Cacheable(value = "usuarios", key = "#id")
    @Override
    public User findById(long id) {
        return userRepository.findById(id).orElse(null);
    }
    @Override
    @Cacheable(value = "usuarios", key = "#email")
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("Usuario no encontrado");
            return new UsernameNotFoundException("Usuario no encontrado");
        });
    }

    @Override
    @Cacheable(value = "usuarios", key = "#email")
    public User findByUsername(String userName) {
        return userRepository.findByUserName(userName).orElseThrow(() -> {
            log.error("Usuario no encontrado");
            return new  UsernameNotFoundException("Usuario no encontrado");
        });
    }

    @Override
    @Cacheable(value = "usuarios")
    public List<User> findAll() {
        return userRepository.findAllActive();
    }

    @Override
    @CacheEvict(value = "usuarios", allEntries = true)
    public User edit(User u) {
        return userRepository.save(u);
    }


    @Override
    @CacheEvict(value = "usuarios", allEntries = true)
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public void deleteLogical(Long id) {
        userRepository.softDelete(id);
    }

    @Override
    public User findByIdOptional(Long id) {
        return userRepository.findActiveById(id);
    }

    @Override
    @CacheEvict(value = "usuarios", allEntries = true)
    public void softDelete(Long id) {
        userRepository.softDelete(id);
    }

    // Pagination methods
    @Override
    public Page<User> findAllPaginated(Optional<String> userNameOrEmail, Pageable pageable) {
        if (userNameOrEmail.isPresent()) {
            if (userNameOrEmail.get().contains("@")) {
                Specification<User> specNameUser = (root, query, criteriaBuilder) ->
                        userNameOrEmail.map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                                        "%" + n.toLowerCase() + "%"))
                                .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
                Specification<User> criterio = Specification.allOf(
                        specNameUser
                );
                return userRepository.findAll(criterio, pageable);
            } else {

                Specification<User> specNameUser = (root, query, criteriaBuilder) ->
                        userNameOrEmail.map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("userName")),
                                        "%" + n.toLowerCase() + "%"))
                                .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
                Specification<User> criterio = Specification.allOf(
                        specNameUser
                );
                return userRepository.findAll(criterio, pageable);
            }
        }
        return userRepository.findAll(pageable);
    }
}
