package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.users.models.Role;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository repositorio;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private String username;
    private String email;

    @BeforeEach
    void setUp() {
        username = "testUser";
        email = "test@example.com";

        testUser = User.builder()
                .id(1L)
                .userName(username)
                .email(email)
                .password("hashedPassword123")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void loadUserByUsernamereturnUserDetailswhenUsernameExistsAndNotDeleted() {
        when(repositorio.findByUserNameAndIsDeletedFalse(username)).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(repositorio).findByUserNameAndIsDeletedFalse(username);
        verify(repositorio, never()).findByEmailAndIsDeletedFalse(anyString());
    }

    @Test
    void loadUserByUsernamereturnUserDetailswhenEmailExistsAndNotDeleted() {
        when(repositorio.findByEmailAndIsDeletedFalse(email)).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(repositorio).findByEmailAndIsDeletedFalse(email);
        verify(repositorio, never()).findByUserNameAndIsDeletedFalse(anyString());
    }

    @Test
    void loadUserByUsernamethrowUsernameNotFoundExceptionwhenUsernameDoesNotExist() {
        when(repositorio.findByUserNameAndIsDeletedFalse(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(username));

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        assertTrue(exception.getMessage().contains(username));
        verify(repositorio).findByUserNameAndIsDeletedFalse(username);
    }

    @Test
    void loadUserByUsernamethrowUsernameNotFoundExceptionwhenEmailDoesNotExist() {
        when(repositorio.findByEmailAndIsDeletedFalse(email)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email));

        assertTrue(exception.getMessage().contains("Email no encontrado"));
        assertTrue(exception.getMessage().contains(email));
        verify(repositorio).findByEmailAndIsDeletedFalse(email);
    }

    @Test
    void loadUserByUsernamesearchByEmailwhenValueContainsAtSymbol() {
        String emailValue = "user@domain.com";
        when(repositorio.findByEmailAndIsDeletedFalse(emailValue)).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername(emailValue);

        assertNotNull(result);
        verify(repositorio).findByEmailAndIsDeletedFalse(emailValue);
        verify(repositorio, never()).findByUserNameAndIsDeletedFalse(anyString());
    }

    @Test
    void loadUserByUsernamesearchByUsernamewhenValueDoesNotContainAtSymbol() {
        String usernameValue = "simpleUsername";
        when(repositorio.findByUserNameAndIsDeletedFalse(usernameValue)).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername(usernameValue);

        assertNotNull(result);
        verify(repositorio).findByUserNameAndIsDeletedFalse(usernameValue);
        verify(repositorio, never()).findByEmailAndIsDeletedFalse(anyString());
    }

    @Test
    void loadUserByUsernamethrowUsernameNotFoundExceptionwhenUserIsDeleted() {
        when(repositorio.findByUserNameAndIsDeletedFalse(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(username));

        verify(repositorio).findByUserNameAndIsDeletedFalse(username);
    }

    @Test
    void loadUserByUsernamedistinguishBetweenEmailAndUsernamewhenBothLookSimilar() {
        String emailLike = "test@company";
        when(repositorio.findByEmailAndIsDeletedFalse(emailLike)).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername(emailLike);

        assertNotNull(result);
        verify(repositorio).findByEmailAndIsDeletedFalse(emailLike);
        verify(repositorio, never()).findByUserNameAndIsDeletedFalse(anyString());
    }

    @Test
    void loadUserByUsernamehandleComplexEmailwhenEmailHasMultipleAtSymbols() {
        String complexEmail = "user+tag@sub.domain.com";
        when(repositorio.findByEmailAndIsDeletedFalse(complexEmail)).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername(complexEmail);

        assertNotNull(result);
        verify(repositorio).findByEmailAndIsDeletedFalse(complexEmail);
        verify(repositorio, never()).findByUserNameAndIsDeletedFalse(anyString());
    }

    @Test
    void loadUserByUsernamereturnUserWithAllPropertieswhenUserFound() {
        testUser.getRoles().add(Role.ADMIN);
        when(repositorio.findByUserNameAndIsDeletedFalse(username)).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertEquals(testUser.getAuthorities(), result.getAuthorities());
        verify(repositorio).findByUserNameAndIsDeletedFalse(username);
    }
}
