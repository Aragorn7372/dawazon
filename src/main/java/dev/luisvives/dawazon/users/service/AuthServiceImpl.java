package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.cart.models.Address;
import dev.luisvives.dawazon.cart.models.Client;
import dev.luisvives.dawazon.common.storage.service.FileSystemStorageService;
import dev.luisvives.dawazon.common.storage.service.StorageService;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.users.dto.UserAdminRequestDto;
import dev.luisvives.dawazon.users.dto.UserChangePasswordDto;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.exceptions.UserException;
import dev.luisvives.dawazon.users.models.Role;
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
        val user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (passwordEncoder.matches(userDto.getOldPassword(), user.getPassword())
                && passwordEncoder.matches(userDto.getNewPassword(), user.getPassword())
                && userDto.getNewPassword().equals(userDto.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(userDto.getNewPassword()));
            return userRepository.save(user);
        } else {
            throw new UserException.UserPasswordNotMatchException("contraseña incorrecta");
        }
    }

    @Override
    public User updateCurrentUser(Long id, UserRequestDto updateUser) {
        log.info("[AuthService.updateCurrentUser] User ID: {}", id);
        log.info("[AuthService.updateCurrentUser] Actualizar datos: nombre={}, email={}, telefono={}",
                updateUser.getNombre(), updateUser.getEmail(), updateUser.getTelefono());

        try {
            var user = userRepository.findById(id).orElseThrow(() -> {
                log.error("[AuthService.updateCurrentUser] Usuario no encontrado con ID: {}", id);
                return new UsernameNotFoundException("User not found");
            });

            log.info("[AuthService.updateCurrentUser] usuario encontrado: ID={}, currentUsername={}, currentEmail={}",
                    user.getId(), user.getUsername(), user.getEmail());

            // Update basic user fields
            user.setUserName(updateUser.getNombre());
            user.setEmail(updateUser.getEmail());
            user.setTelefono(updateUser.getTelefono());

            // Create or update Client data
            if (updateUser.getCalle() != null || updateUser.getCiudad() != null ||
                    updateUser.getCodigoPostal() != null || updateUser.getProvincia() != null) {

                log.info("[AuthService.updateCurrentUser] Actualizando datos de cliente");

                // Create or get existing client
                Client client = user.getClient();
                if (client == null) {
                    client = new Client();
                    log.info("[AuthService.updateCurrentUser] nuevo Client");
                }

                // Update client fields
                client.setName(updateUser.getNombre());
                client.setEmail(updateUser.getEmail());
                client.setPhone(updateUser.getTelefono());

                // Create or update address
                Address address = client.getAddress();
                if (address == null) {
                    address = new Address();
                    log.info("[AuthService.updateCurrentUser] nuevo Address");
                }

                address.setStreet(updateUser.getCalle());
                address.setCity(updateUser.getCiudad());
                // Convert String to Integer for postalCode
                if (updateUser.getCodigoPostal() != null && !updateUser.getCodigoPostal().isEmpty()) {
                    try {
                        address.setPostalCode(Integer.parseInt(updateUser.getCodigoPostal()));
                    } catch (NumberFormatException e) {
                        log.warn("[AuthService.updateCurrentUser] formato de código postal inválido: {}",
                                updateUser.getCodigoPostal());
                    }
                }
                address.setProvince(updateUser.getProvincia());

                client.setAddress(address);
                user.setClient(client);

                log.info("[AuthService.updateCurrentUser] Client y Address actualizados");
            }

            log.info("[AuthService.updateCurrentUser] Guardando usuario...");
            User savedUser = userRepository.save(user);
            log.info("[AuthService.updateCurrentUser] Usuario guardado con éxito: ID={}, Username={}",
                    savedUser.getId(), savedUser.getUsername());

            return savedUser;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public User updateAdminCurrentUser(Long id, UserAdminRequestDto updateUser) {
        var user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User no encontrado"));
        user.setUserName(updateUser.getNombre());
        user.setEmail(updateUser.getEmail());
        user.setRoles(List.of(Role.valueOf(updateUser.getRoles())));
        user.setTelefono(updateUser.getTelefono());
        return userRepository.save(user);
    }

    @Override
    public User updateImage(Long id, MultipartFile image) {
        log.info("[AuthService.updateImage] User ID: {}", id);
        log.info("[AuthService.updateImage] Image file: name={}, size={}, contentType={}",
                image != null ? image.getOriginalFilename() : "null",
                image != null ? image.getSize() : 0,
                image != null ? image.getContentType() : "null");

        try {
            val user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("[AuthService.updateImage] Usuario no encontrado con id: {}", id);
                        return new ProductException.NotFoundException("Usuario no encontrado con id: " + id);
                    });

            log.info("[AuthService.updateImage] Usuario encontrado: ID={}, currentAvatar={}",
                    user.getId(), user.getAvatar());

            if (user.getAvatar() != null && !user.getAvatar().equals(IMAGE_DEFAULT)) {
                log.info("[AuthService.updateImage] Eliminando avatar antiguo: {}", user.getAvatar());
                storage.delete(user.getAvatar());
            }

            log.info("[AuthService.updateImage] Guardando imagen...");
            String imageStored = storage.store(image);
            log.info("[AuthService.updateImage] Imagen guardada con éxito: {}", imageStored);

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

            log.info("[AuthService.updateImage] Guardadno usuario con nuevo avatar...");
            User savedUser = userRepository.save(userUpdated);
            log.info("[AuthService.updateImage] User guardado correctamente con avatar: {}", savedUser.getAvatar());

            return savedUser;
        } catch (Exception e) {
            throw e;
        }
    }

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
            return new UsernameNotFoundException("Usuario no encontrado");
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
                Specification<User> specNameUser = (root, query, criteriaBuilder) -> userNameOrEmail
                        .map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                                "%" + n.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
                Specification<User> criterio = Specification.allOf(
                        specNameUser);
                return userRepository.findAll(criterio, pageable);
            } else {

                Specification<User> specNameUser = (root, query, criteriaBuilder) -> userNameOrEmail
                        .map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("userName")),
                                "%" + n.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
                Specification<User> criterio = Specification.allOf(
                        specNameUser);
                return userRepository.findAll(criterio, pageable);
            }
        }
        return userRepository.findAll(pageable);
    }
}
