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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.ArrayList;
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

/**
 * Implementación del servicio de autenticación y gestión de usuarios.
 * <p>
 * Proporciona operaciones CRUD completas, gestión de contraseñas,
 * actualización de perfiles de usuario y administrador, y manejo de imágenes de
 * avatar.
 * Utiliza caché para optimizar consultas frecuentes.
 * </p>
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final StorageService storage;
    BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param repositorio     Repositorio de usuarios
     * @param passwordEncoder Codificador de contraseñas BCrypt
     * @param userRepository  Repositorio de usuarios (inyección adicional)
     * @param storage         Servicio de almacenamiento de archivos
     */
    @Autowired
    public AuthServiceImpl(UserRepository repositorio,
            BCryptPasswordEncoder passwordEncoder,
            UserRepository userRepository,
            FileSystemStorageService storage) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.storage = storage;
    }

    /**
     * Registra un nuevo usuario encriptando su contraseña.
     *
     * @param u Usuario a registrar
     * @return Usuario guardado con contraseña encriptada
     */
    @CacheEvict(value = "usuarios", allEntries = true)
    @Override
    public User register(User u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return userRepository.save(u);
    }

    /**
     * Cambia la contraseña de un usuario.
     * <p>
     * Verifica que la contraseña antigua coincida y que la nueva contraseña
     * esté confirmada correctamente antes de realizar el cambio.
     * </p>
     *
     * @param userDto DTO con contraseñas (antigua, nueva, confirmación)
     * @param id      ID del usuario
     * @return Usuario con contraseña actualizada
     * @throws UsernameNotFoundException                   Si el usuario no existe
     * @throws UserException.UserPasswordNotMatchException Si las contraseñas no
     *                                                     coinciden
     */
    @CacheEvict(value = "usuarios", allEntries = true)
    @Override
    public User changePassword(UserChangePasswordDto userDto, Long id) {
        val user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (passwordEncoder.matches(userDto.getOldPassword(), user.getPassword())
                && !passwordEncoder.matches(userDto.getNewPassword(), user.getPassword())
                && userDto.getNewPassword().equals(userDto.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(userDto.getNewPassword()));
            return userRepository.save(user);
        } else {
            throw new UserException.UserPasswordNotMatchException("contraseña incorrecta");
        }
    }

    /**
     * Actualiza los datos del usuario actual (perfil de usuario regular).
     * <p>
     * Permite actualizar nombre de usuario, email, teléfono y datos del cliente
     * (incluyendo dirección). Crea automáticamente objetos Client y Address si no
     * existen.
     * </p>
     *
     * @param id         ID del usuario a actualizar
     * @param updateUser DTO con datos a actualizar
     * @return Usuario actualizado
     * @throws UsernameNotFoundException Si el usuario no existe
     */
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

            user.setUserName(updateUser.getNombre());
            user.setEmail(updateUser.getEmail());
            user.setTelefono(updateUser.getTelefono());

            if (updateUser.getCalle() != null || updateUser.getCiudad() != null ||
                    updateUser.getCodigoPostal() != null || updateUser.getProvincia() != null) {

                log.info("[AuthService.updateCurrentUser] Actualizando datos de cliente");

                Client client = user.getClient();
                if (client == null) {
                    client = new Client();
                    log.info("[AuthService.updateCurrentUser] nuevo Client");
                }

                client.setName(updateUser.getNombre());
                client.setEmail(updateUser.getEmail());
                client.setPhone(updateUser.getTelefono());
                Address address = client.getAddress();
                if (address == null) {
                    address = new Address();
                    log.info("[AuthService.updateCurrentUser] nuevo Address");
                }

                address.setStreet(updateUser.getCalle());
                address.setCity(updateUser.getCiudad());
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

    /**
     * Actualiza los datos del usuario desde el panel de administración.
     * <p>
     * Similar a updateCurrentUser pero con capacidad adicional de actualizar roles
     * del usuario.
     * Permite gestión completa del perfil desde administración.
     * </p>
     *
     * @param id         ID del usuario a actualizar
     * @param updateUser DTO con datos a actualizar (incluye roles)
     * @return Usuario actualizado
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Override
    public User updateAdminCurrentUser(Long id, UserAdminRequestDto updateUser) {
        log.info("[AuthService.updateAdminCurrentUser] User ID: {}", id);
        log.info("[AuthService.updateAdminCurrentUser] Actualizar datos: nombre={}, email={}, telefono={}",
                updateUser.getNombre(), updateUser.getEmail(), updateUser.getTelefono());

        try {
            var user = userRepository.findById(id).orElseThrow(() -> {
                log.error("[AuthService.updateAdminCurrentUser] Usuario no encontrado con ID: {}", id);
                return new UsernameNotFoundException("User no encontrado");
            });

            log.info(
                    "[AuthService.updateAdminCurrentUser] usuario encontrado: ID={}, currentUsername={}, currentEmail={}",
                    user.getId(), user.getUsername(), user.getEmail());

            user.setUserName(updateUser.getNombre());
            user.setEmail(updateUser.getEmail());
            user.setTelefono(updateUser.getTelefono());

            if (updateUser.getRoles() != null && !updateUser.getRoles().isEmpty()) {
                log.info("[AuthService.updateAdminCurrentUser] Updating roles from {} to {}",
                        user.getRoles(), updateUser.getRoles());
                user.setRoles(new ArrayList<>(
                        updateUser.getRoles().stream()
                                .map(Role::valueOf)
                                .toList()));
                log.info("[AuthService.updateAdminCurrentUser] Roles updated to: {}", user.getRoles());
            } else {
                log.warn("[AuthService.updateAdminCurrentUser] No roles provided in DTO");
            }

            if (updateUser.getCalle() != null || updateUser.getCiudad() != null ||
                    updateUser.getCodigoPostal() != null || updateUser.getProvincia() != null) {

                log.info("[AuthService.updateAdminCurrentUser] Actualizando datos de cliente");

                Client client = user.getClient();
                if (client == null) {
                    client = new Client();
                    log.info("[AuthService.updateAdminCurrentUser] nuevo Client");
                }

                client.setName(updateUser.getNombre());
                client.setEmail(updateUser.getEmail());
                client.setPhone(updateUser.getTelefono());

                Address address = client.getAddress();
                if (address == null) {
                    address = new Address();
                    log.info("[AuthService.updateAdminCurrentUser] nuevo Address");
                }

                address.setStreet(updateUser.getCalle());
                address.setCity(updateUser.getCiudad());
                if (updateUser.getCodigoPostal() != null && !updateUser.getCodigoPostal().isEmpty()) {
                    try {
                        address.setPostalCode(Integer.parseInt(updateUser.getCodigoPostal()));
                    } catch (NumberFormatException e) {
                        log.warn("[AuthService.updateAdminCurrentUser] formato de código postal inválido: {}",
                                updateUser.getCodigoPostal());
                    }
                }
                address.setProvince(updateUser.getProvincia());

                client.setAddress(address);
                user.setClient(client);

                log.info("[AuthService.updateAdminCurrentUser] Client y Address actualizados");
            }

            log.info("[AuthService.updateAdminCurrentUser] Guardando usuario...");
            User savedUser = userRepository.save(user);
            log.info("[AuthService.updateAdminCurrentUser] Usuario guardado con éxito: ID={}, Username={}",
                    savedUser.getId(), savedUser.getUsername());

            return savedUser;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Actualiza la imagen de avatar de un usuario.
     * <p>
     * Elimina la imagen anterior (si no es la predeterminada), guarda la nueva
     * imagen
     * en el almacenamiento y actualiza la referencia en el usuario.
     * </p>
     *
     * @param id    ID del usuario
     * @param image Archivo de imagen nuevo
     * @return Usuario con avatar actualizado
     * @throws ProductException.NotFoundException Si el usuario no existe
     */
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

    /**
     * Busca un usuario por su ID.
     *
     * @param id ID del usuario
     * @return Usuario encontrado o null si no existe
     */
    @Override
    public User findById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Busca un usuario por email con caché.
     *
     * @param email Email del usuario
     * @return Usuario encontrado
     * @throws UsernameNotFoundException Si no se encuentra el usuario
     */
    @Override
    @Cacheable(value = "usuarios", key = "#email")
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("Usuario no encontrado");
            return new UsernameNotFoundException("Usuario no encontrado");
        });
    }

    /**
     * Busca un usuario por nombre de usuario con caché.
     *
     * @param userName Nombre de usuario
     * @return Usuario encontrado
     * @throws UsernameNotFoundException Si no se encuentra el usuario
     */
    @Override
    @Cacheable(value = "usuarios", key = "#email")
    public User findByUsername(String userName) {
        return userRepository.findByUserName(userName).orElseThrow(() -> {
            log.error("Usuario no encontrado");
            return new UsernameNotFoundException("Usuario no encontrado");
        });
    }

    /**
     * Obtiene todos los usuarios activos (no eliminados lógicamente) con caché.
     *
     * @return Lista de usuarios activos
     */
    @Override
    @Cacheable(value = "usuarios")
    public List<User> findAll() {
        return userRepository.findAllActive();
    }

    /**
     * Edita/actualiza un usuario e invalida el caché.
     *
     * @param u Usuario a actualizar
     * @return Usuario actualizado
     */
    @Override
    @CacheEvict(value = "usuarios", allEntries = true)
    public User edit(User u) {
        return userRepository.save(u);
    }

    /**
     * Elimina físicamente un usuario e invalida el caché.
     *
     * @param id ID del usuario a eliminar
     */
    @Override
    @CacheEvict(value = "usuarios", allEntries = true)
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Elimina lógicamente un usuario (marca isDeleted=true).
     *
     * @param id ID del usuario
     */
    public void deleteLogical(Long id) {
        userRepository.softDelete(id);
    }

    /**
     * Busca un usuario activo por ID (no deletado lógicamente).
     *
     * @param id ID del usuario
     * @return Usuario activo o null
     */
    @Override
    public User findByIdOptional(Long id) {
        return userRepository.findActiveById(id);
    }

    /**
     * Elimina lógicamente un usuario e invalida el caché.
     *
     * @param id ID del usuario
     */
    @Override
    @CacheEvict(value = "usuarios", allEntries = true)
    @Transactional
    public void softDelete(Long id) {
        userRepository.softDelete(id);
    }

    /**
     * Busca usuarios de forma paginada con filtro opcional por nombre o email.
     * <p>
     * Detecta automáticamente si el filtro es un email (contiene @) o nombre de
     * usuario
     * y aplica la búsqueda correspondiente usando Specifications.
     * </p>
     *
     * @param userNameOrEmail Filtro opcional: nombre de usuario o email
     * @param pageable        Configuración de paginación
     * @return Página de usuarios
     */
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
