package dev.luisvives.dawazon.users.repository;

import dev.luisvives.dawazon.BaseRepositoryTest;
import dev.luisvives.dawazon.users.models.Role;
import dev.luisvives.dawazon.users.models.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser1;
    private User testUser2;
    private User deletedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser1 = User.builder()
                .userName("testUser1")
                .email("test1@example.com")
                .password("password123")
                .telefono("123456789")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .userName("testUser2")
                .email("test2@example.com")
                .password("password456")
                .telefono("987654321")
                .roles(new ArrayList<>(List.of(Role.USER, Role.ADMIN)))
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        deletedUser = User.builder()
                .userName("deletedUser")
                .email("deleted@example.com")
                .password("password789")
                .telefono("111222333")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .isDeleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(deletedUser);
    }

    @Test
    void findByUserNameAndIsDeletedFalse_returnUser_whenUserExistsAndNotDeleted() {
        Optional<User> result = userRepository.findByUserNameAndIsDeletedFalse("testUser1");

        assertTrue(result.isPresent());
        assertEquals("testUser1", result.get().getUsername());
        assertEquals("test1@example.com", result.get().getEmail());
        assertFalse(result.get().isDeleted());
    }

    @Test
    void findByUserNameAndIsDeletedFalse_returnEmpty_whenUserDoesNotExist() {
        Optional<User> result = userRepository.findByUserNameAndIsDeletedFalse("nonExistentUser");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserNameAndIsDeletedFalse_returnEmpty_whenUserIsDeleted() {
        Optional<User> result = userRepository.findByUserNameAndIsDeletedFalse("deletedUser");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserName_returnUser_whenUserExists() {
        Optional<User> result = userRepository.findByUserName("testUser1");

        assertTrue(result.isPresent());
        assertEquals("testUser1", result.get().getUsername());
    }

    @Test
    void findByUserName_returnDeletedUser_whenUserIsDeleted() {
        Optional<User> result = userRepository.findByUserName("deletedUser");

        assertTrue(result.isPresent());
        assertEquals("deletedUser", result.get().getUsername());
        assertTrue(result.get().isDeleted());
    }

    @Test
    void findByEmailAndIsDeletedFalse_returnUser_whenEmailExistsAndNotDeleted() {
        Optional<User> result = userRepository.findByEmailAndIsDeletedFalse("test1@example.com");

        assertTrue(result.isPresent());
        assertEquals("testUser1", result.get().getUsername());
        assertEquals("test1@example.com", result.get().getEmail());
        assertFalse(result.get().isDeleted());
    }

    @Test
    void findByEmailAndIsDeletedFalse_returnEmpty_whenEmailDoesNotExist() {
        Optional<User> result = userRepository.findByEmailAndIsDeletedFalse("nonexistent@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmailAndIsDeletedFalse_returnEmpty_whenUserWithEmailIsDeleted() {
        Optional<User> result = userRepository.findByEmailAndIsDeletedFalse("deleted@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_returnUser_whenEmailExists() {
        Optional<User> result = userRepository.findByEmail("test1@example.com");

        assertTrue(result.isPresent());
        assertEquals("testUser1", result.get().getUsername());
    }

    @Test
    void findByEmail_returnDeletedUser_whenEmailExistsForDeletedUser() {
        Optional<User> result = userRepository.findByEmail("deleted@example.com");

        assertTrue(result.isPresent());
        assertEquals("deletedUser", result.get().getUsername());
        assertTrue(result.get().isDeleted());
    }

    @Test
    void findAllActive_returnOnlyNonDeletedUsers_whenUsersExist() {
        List<User> result = userRepository.findAllActive();

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(User::isDeleted));
        assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("testUser1")));
        assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("testUser2")));
        assertFalse(result.stream().anyMatch(u -> u.getUsername().equals("deletedUser")));
    }

    @Test
    void findAllActive_returnEmptyList_whenNoActiveUsersExist() {
        userRepository.deleteAll();
        User onlyDeletedUser = User.builder()
                .userName("onlyDeleted")
                .email("only@deleted.com")
                .password("pass")
                .isDeleted(true)
                .build();
        userRepository.save(onlyDeletedUser);

        List<User> result = userRepository.findAllActive();

        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveById_returnUser_whenUserExistsAndNotDeleted() {
        Long userId = testUser1.getId();

        User result = userRepository.findActiveById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testUser1", result.getUsername());
        assertFalse(result.isDeleted());
    }

    @Test
    void findActiveById_returnNull_whenUserDoesNotExist() {
        User result = userRepository.findActiveById(999L);

        assertNull(result);
    }

    @Test
    void findActiveById_returnNull_whenUserIsDeleted() {
        Long deletedUserId = deletedUser.getId();

        User result = userRepository.findActiveById(deletedUserId);

        assertNull(result);
    }

    @Test
    void softDelete_markUserAsDeleted_whenUserExists() {
        Long userId = testUser1.getId();

        userRepository.softDelete(userId);
        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findById(userId);
        assertTrue(result.isPresent());
        assertTrue(result.get().isDeleted());
    }

    @Test
    void softDelete_notAffectOtherUsers_whenDeletingOneUser() {
        Long userId = testUser1.getId();
        Long otherUserId = testUser2.getId();

        userRepository.softDelete(userId);
        entityManager.flush();
        entityManager.clear();

        Optional<User> deletedUserResult = userRepository.findById(userId);
        Optional<User> otherUserResult = userRepository.findById(otherUserId);

        assertTrue(deletedUserResult.isPresent());
        assertTrue(deletedUserResult.get().isDeleted());

        assertTrue(otherUserResult.isPresent());
        assertFalse(otherUserResult.get().isDeleted());
    }

    @Test
    void findAll_returnPagedResults_whenUsingSpecificationAndPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isDeleted"),
                false);

        Page<User> result = userRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findAll_filterByUsername_whenUsingSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder
                .like(criteriaBuilder.lower(root.get("userName")), "%testuser1%");

        Page<User> result = userRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testUser1", result.getContent().get(0).getUsername());
    }

    @Test
    void findAll_supportPagination_whenMultiplePagesExist() {
        // Crear más usuarios para probar paginación
        for (int i = 3; i <= 10; i++) {
            User user = User.builder()
                    .userName("user" + i)
                    .email("user" + i + "@example.com")
                    .password("pass" + i)
                    .isDeleted(false)
                    .build();
            userRepository.save(user);
        }

        Pageable firstPage = PageRequest.of(0, 5);
        Pageable secondPage = PageRequest.of(1, 5);

        Page<User> page1 = userRepository.findAll(firstPage);
        Page<User> page2 = userRepository.findAll(secondPage);

        assertEquals(5, page1.getContent().size());
        assertEquals(5, page2.getContent().size());
        assertTrue(page1.getTotalElements() >= 10);
        assertNotEquals(page1.getContent().get(0).getId(), page2.getContent().get(0).getId());
    }

    @Test
    void save_persistUser_whenValidUserProvided() {
        User newUser = User.builder()
                .userName("newUser")
                .email("new@example.com")
                .password("newPassword")
                .telefono("555555555")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(newUser);

        assertNotNull(saved.getId());
        assertEquals("newUser", saved.getUsername());

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("newUser", found.get().getUsername());
    }

    @Test
    void save_updateExistingUser_whenUserAlreadyExists() {
        testUser1.setEmail("updated@example.com");
        testUser1.setTelefono("999999999");

        User updated = userRepository.save(testUser1);

        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("999999999", updated.getTelefono());

        Optional<User> found = userRepository.findById(testUser1.getId());
        assertTrue(found.isPresent());
        assertEquals("updated@example.com", found.get().getEmail());
    }

    @Test
    void deleteById_removeUserPermanently_whenUserExists() {
        Long userId = testUser1.getId();

        userRepository.deleteById(userId);

        Optional<User> result = userRepository.findById(userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_returnUser_whenUserExists() {
        Long userId = testUser1.getId();

        Optional<User> result = userRepository.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    void findById_returnDeletedUser_whenUserIsDeleted() {
        Long deletedUserId = deletedUser.getId();

        Optional<User> result = userRepository.findById(deletedUserId);

        assertTrue(result.isPresent());
        assertTrue(result.get().isDeleted());
    }

    @Test
    void count_returnCorrectCount_whenUsersExist() {
        long count = userRepository.count();

        assertEquals(3, count); // testUser1, testUser2, deletedUser
    }
}
