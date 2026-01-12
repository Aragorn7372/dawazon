package dev.luisvives.dawazon.users.repository;

import dev.luisvives.dawazon.users.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de usuarios.
 * <p>
 * Proporciona operaciones CRUD y consultas personalizadas con borrado lógico.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Busca un usuario activo por nombre de usuario.
     *
     * @param value Nombre de usuario.
     * @return Usuario opcional.
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted=false AND u.userName= :value")
    Optional<User> findByUserNameAndIsDeletedFalse(String value);

    /**
     * Busca un usuario por nombre de usuario.
     *
     * @param value Nombre de usuario.
     * @return Usuario opcional.
     */
    Optional<User> findByUserName(String value);

    /**
     * Busca un usuario activo por email.
     *
     * @param email Email del usuario.
     * @return Usuario opcional.
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted=false AND u.email= :email")
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * Busca un usuario por email.
     *
     * @param email Email del usuario.
     * @return Usuario opcional.
     */
    Optional<User> findByEmail(String email);

    /**
     * Obtiene todos los usuarios activos (no borrados).
     *
     * @return Lista de usuarios activos.
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted=false")
    List<User> findAllActive();

    /**
     * Busca un usuario activo por ID.
     *
     * @param id ID del usuario.
     * @return Usuario activo o null.
     */
    @Query("SELECT u FROM  User u WHERE u.id= :id AND u.isDeleted=false")
    User findActiveById(Long id);

    /**
     * Realiza un borrado lógico de un usuario.
     *
     * @param id ID del usuario a eliminar.
     */
    @Modifying
    @Query("UPDATE User u SET u.isDeleted=true WHERE u.id=:id")
    void softDelete(Long id);

    /**
     * Busca usuarios con criterios y paginación.
     *
     * @param criterio Especificación de criterios de búsqueda.
     * @param pageable Configuración de paginación.
     * @return Página de usuarios.
     */
    Page<User> findAll(Specification<User> criterio, Pageable pageable);
}
