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

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String value);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.isDeleted=false")
    List<User> findAllActive();

    @Query("SELECT u FROM  User u WHERE u.id= :id AND u.isDeleted=false")
    User findActiveById(Long id);

    @Modifying
    @Query("UPDATE User u SET u.isDeleted=true WHERE u.id=:id")
    Optional<User> softDelete(Long id);

    Page<User> findAll(Specification<User> criterio, Pageable pageable);
}
