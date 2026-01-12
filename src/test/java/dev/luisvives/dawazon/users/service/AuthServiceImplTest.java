package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.cart.models.Address;
import dev.luisvives.dawazon.cart.models.Client;
import dev.luisvives.dawazon.common.storage.service.FileSystemStorageService;
import dev.luisvives.dawazon.products.exception.ProductException;
import dev.luisvives.dawazon.users.dto.UserAdminRequestDto;
import dev.luisvives.dawazon.users.dto.UserChangePasswordDto;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.exceptions.UserException;
import dev.luisvives.dawazon.users.models.Role;
import dev.luisvives.dawazon.users.models.User;
import dev.luisvives.dawazon.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.luisvives.dawazon.users.models.User.IMAGE_DEFAULT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileSystemStorageService storage;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserRequestDto userRequestDto;
    private UserAdminRequestDto userAdminRequestDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .userName("testUser")
                .email("test@example.com")
                .telefono("123456789")
                .password("hashedPassword")
                .avatar(IMAGE_DEFAULT)
                .roles(new ArrayList<>(List.of(Role.USER)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRequestDto = UserRequestDto.builder()
                .nombre("updatedName")
                .email("updated@example.com")
                .telefono("987654321")
                .build();

        userAdminRequestDto = UserAdminRequestDto.builder()
                .id(1L)
                .nombre("adminUpdatedName")
                .email("adminUpdated@example.com")
                .telefono("111222333")
                .roles(Set.of("USER", "ADMIN"))
                .build();
    }

    @Test
    void registerencryptPasswordAndSaveUserwhenValidUserProvided() {
        String rawPassword = "rawPassword123";
        String encodedPassword = "encodedPassword123";
        User newUser = User.builder()
                .userName("newUser")
                .email("new@example.com")
                .password(rawPassword)
                .build();

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = authService.register(newUser);

        assertNotNull(result);
        assertEquals(encodedPassword, newUser.getPassword());
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(newUser);
    }

    @Test
    void changePasswordupdatePasswordwhenOldPasswordMatchesAndNewPasswordsMatch() {
        Long userId = 1L;
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String hashedOldPassword = "hashedOldPassword";
        String hashedNewPassword = "hashedNewPassword";

        UserChangePasswordDto changePasswordDto = UserChangePasswordDto.builder()
                .oldPassword(oldPassword)
                .newPassword(newPassword)
                .confirmPassword(newPassword)
                .build();

        testUser.setPassword(hashedOldPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, hashedOldPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, hashedOldPassword)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.changePassword(changePasswordDto, userId);

        assertNotNull(result);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    void changePasswordthrowUsernameNotFoundExceptionwhenUserNotFound() {
        Long userId = 999L;
        UserChangePasswordDto changePasswordDto = UserChangePasswordDto.builder()
                .oldPassword("old")
                .newPassword("new")
                .confirmPassword("new")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.changePassword(changePasswordDto, userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordthrowUserPasswordNotMatchExceptionwhenOldPasswordDoesNotMatch() {
        Long userId = 1L;
        UserChangePasswordDto changePasswordDto = UserChangePasswordDto.builder()
                .oldPassword("wrongOldPassword")
                .newPassword("newPassword")
                .confirmPassword("newPassword")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOldPassword", testUser.getPassword())).thenReturn(false);

        assertThrows(UserException.UserPasswordNotMatchException.class,
                () -> authService.changePassword(changePasswordDto, userId));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordthrowUserPasswordNotMatchExceptionwhenConfirmPasswordDoesNotMatch() {
        Long userId = 1L;
        UserChangePasswordDto changePasswordDto = UserChangePasswordDto.builder()
                .oldPassword("oldPassword")
                .newPassword("newPassword")
                .confirmPassword("differentPassword")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPassword", testUser.getPassword())).thenReturn(false);

        assertThrows(UserException.UserPasswordNotMatchException.class,
                () -> authService.changePassword(changePasswordDto, userId));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordthrowUserPasswordNotMatchExceptionwhenNewPasswordEqualsOldPassword() {
        Long userId = 1L;
        String oldPassword = "samePassword123";
        String hashedPassword = "hashedSamePassword";
        UserChangePasswordDto changePasswordDto = UserChangePasswordDto.builder()
                .oldPassword(oldPassword)
                .newPassword(oldPassword)
                .confirmPassword(oldPassword)
                .build();

        testUser.setPassword(hashedPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, hashedPassword)).thenReturn(true);

        assertThrows(UserException.UserPasswordNotMatchException.class,
                () -> authService.changePassword(changePasswordDto, userId));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateCurrentUserupdateBasicFieldswhenNoAddressDataProvided() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateCurrentUser(userId, userRequestDto);

        assertNotNull(result);
        assertEquals(userRequestDto.getNombre(), testUser.getUsername());
        assertEquals(userRequestDto.getEmail(), testUser.getEmail());
        assertEquals(userRequestDto.getTelefono(), testUser.getTelefono());
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateCurrentUsercreateClientAndAddresswhenAddressDataProvidedAndClientIsNull() {
        Long userId = 1L;
        userRequestDto.setCalle("Main Street");
        userRequestDto.setCiudad("Madrid");
        userRequestDto.setCodigoPostal("28001");
        userRequestDto.setProvincia("Madrid");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateCurrentUser(userId, userRequestDto);

        assertNotNull(result);
        assertNotNull(testUser.getClient());
        assertNotNull(testUser.getClient().getAddress());
        assertEquals("Main Street", testUser.getClient().getAddress().getStreet());
        assertEquals("Madrid", testUser.getClient().getAddress().getCity());
        assertEquals(28001, testUser.getClient().getAddress().getPostalCode());
        assertEquals("Madrid", testUser.getClient().getAddress().getProvince());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateCurrentUserupdateExistingClientAndAddresswhenAddressDataProvidedAndClientExists() {
        Long userId = 1L;
        Client existingClient = new Client();
        existingClient.setName("oldName");
        Address existingAddress = new Address();
        existingAddress.setStreet("Old Street");
        existingClient.setAddress(existingAddress);
        testUser.setClient(existingClient);

        userRequestDto.setCalle("New Street");
        userRequestDto.setCiudad("Barcelona");
        userRequestDto.setCodigoPostal("08001");
        userRequestDto.setProvincia("Barcelona");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateCurrentUser(userId, userRequestDto);

        assertNotNull(result);
        assertEquals("New Street", testUser.getClient().getAddress().getStreet());
        assertEquals("Barcelona", testUser.getClient().getAddress().getCity());
        assertEquals(8001, testUser.getClient().getAddress().getPostalCode());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateCurrentUserthrowUsernameNotFoundExceptionwhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.updateCurrentUser(userId, userRequestDto));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateCurrentUseupdateOnlyStreetAndCitywhenPostalCodeAndProvinceAreNull() {
        Long userId = 1L;
        userRequestDto.setCalle("Only Street");
        userRequestDto.setCiudad("Only City");
        userRequestDto.setCodigoPostal(null);
        userRequestDto.setProvincia(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateCurrentUser(userId, userRequestDto);

        assertNotNull(result);
        assertNotNull(testUser.getClient());
        assertNotNull(testUser.getClient().getAddress());
        assertEquals("Only Street", testUser.getClient().getAddress().getStreet());
        assertEquals("Only City", testUser.getClient().getAddress().getCity());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateCurrentUserskipPostalCodeUpdatewhenPostalCodeIsEmpty() {
        Long userId = 1L;
        userRequestDto.setCalle("Street");
        userRequestDto.setCodigoPostal("");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateCurrentUser(userId, userRequestDto);

        assertNotNull(result);
        assertNotNull(testUser.getClient());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateCurrentUserlogWarningAndSkipPostalCodewhenPostalCodeIsInvalid() {
        Long userId = 1L;
        userRequestDto.setCalle("Test Street");
        userRequestDto.setCodigoPostal("INVALID_CODE");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateCurrentUser(userId, userRequestDto);

        assertNotNull(result);
        assertNotNull(testUser.getClient());
        assertNotNull(testUser.getClient().getAddress());
        assertEquals("Test Street", testUser.getClient().getAddress().getStreet());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateAdminCurrentUserupdateUserWithRoleswhenValidAdminRequestProvided() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateAdminCurrentUser(userId, userAdminRequestDto);

        assertNotNull(result);
        assertEquals(userAdminRequestDto.getNombre(), testUser.getUsername());
        assertEquals(userAdminRequestDto.getEmail(), testUser.getEmail());
        assertEquals(2, testUser.getRoles().size());
        assertTrue(testUser.getRoles().contains(Role.USER));
        assertTrue(testUser.getRoles().contains(Role.ADMIN));
        verify(userRepository).save(testUser);
    }

    @Test
    void updateAdminCurrentUsernotUpdateRoleswhenRolesAreEmpty() {
        Long userId = 1L;
        userAdminRequestDto.setRoles(Set.of());
        List<Role> originalRoles = new ArrayList<>(testUser.getRoles());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateAdminCurrentUser(userId, userAdminRequestDto);

        assertNotNull(result);
        assertEquals(originalRoles, testUser.getRoles());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateAdminCurrentUsernotUpdateRoleswhenNoRolesProvided() {
        Long userId = 1L;
        userAdminRequestDto.setRoles(null);
        List<Role> originalRoles = new ArrayList<>(testUser.getRoles());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateAdminCurrentUser(userId, userAdminRequestDto);

        assertNotNull(result);
        assertEquals(originalRoles, testUser.getRoles());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateAdminCurrentUserupdateExistingClientWithoutAddresswhenAddressDataProvided() {
        Long userId = 1L;
        Client existingClient = new Client();
        existingClient.setName("OldClient");
        existingClient.setAddress(null);
        testUser.setClient(existingClient);

        userAdminRequestDto.setCalle("New Admin Street");
        userAdminRequestDto.setCiudad("New Admin City");
        userAdminRequestDto.setCodigoPostal("50001");
        userAdminRequestDto.setProvincia("Zaragoza");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateAdminCurrentUser(userId, userAdminRequestDto);

        assertNotNull(result);
        assertNotNull(testUser.getClient());
        assertNotNull(testUser.getClient().getAddress());
        assertEquals("New Admin Street", testUser.getClient().getAddress().getStreet());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateAdminCurrentUserskipPostalCodeUpdatewhenPostalCodeIsEmpty() {
        Long userId = 1L;
        userAdminRequestDto.setCalle("Admin Street");
        userAdminRequestDto.setCodigoPostal("");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateAdminCurrentUser(userId, userAdminRequestDto);

        assertNotNull(result);
        assertNotNull(testUser.getClient());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateAdminCurrentUserlogWarningAndSkipPostalCodewhenPostalCodeIsInvalid() {
        Long userId = 1L;
        userAdminRequestDto.setCalle("Admin Street");
        userAdminRequestDto.setCodigoPostal("NOT_A_NUMBER");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateAdminCurrentUser(userId, userAdminRequestDto);

        assertNotNull(result);
        assertNotNull(testUser.getClient());
        assertNotNull(testUser.getClient().getAddress());
        assertEquals("Admin Street", testUser.getClient().getAddress().getStreet());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateAdminCurrentUsercreateClientAndAddresswhenAddressDataProvided() {
        Long userId = 1L;
        userAdminRequestDto.setCalle("Admin Street");
        userAdminRequestDto.setCiudad("Valencia");
        userAdminRequestDto.setCodigoPostal("46001");
        userAdminRequestDto.setProvincia("Valencia");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateAdminCurrentUser(userId, userAdminRequestDto);

        assertNotNull(result);
        assertNotNull(testUser.getClient());
        assertNotNull(testUser.getClient().getAddress());
        assertEquals("Admin Street", testUser.getClient().getAddress().getStreet());
        assertEquals("Valencia", testUser.getClient().getAddress().getCity());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateAdminCurrentUserthrowUsernameNotFoundExceptionwhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authService.updateAdminCurrentUser(userId, userAdminRequestDto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateImagereplaceOldImageAndSaveNewOnewhenNonDefaultAvatarExists() {
        Long userId = 1L;
        String oldAvatar = "old-avatar.jpg";
        String newImageName = "new-avatar.jpg";
        testUser.setAvatar(oldAvatar);

        MultipartFile mockFile = new MockMultipartFile("file", "avatar.jpg",
                "image/jpeg", "test image content".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(storage.store(mockFile)).thenReturn(newImageName);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateImage(userId, mockFile);

        assertNotNull(result);
        verify(storage).delete(oldAvatar);
        verify(storage).store(mockFile);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateImagenotDeleteOldImagewhenAvatarIsNull() {
        Long userId = 1L;
        String newImageName = "new-avatar.jpg";
        testUser.setAvatar(null);

        MultipartFile mockFile = new MockMultipartFile("file", "avatar.jpg",
                "image/jpeg", "test image content".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(storage.store(mockFile)).thenReturn(newImageName);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateImage(userId, mockFile);

        assertNotNull(result);
        verify(storage, never()).delete(anyString());
        verify(storage).store(mockFile);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateImagenotDeleteOldImagewhenDefaultAvatarIsSet() {
        Long userId = 1L;
        String newImageName = "new-avatar.jpg";
        testUser.setAvatar(IMAGE_DEFAULT);

        MultipartFile mockFile = new MockMultipartFile("file", "avatar.jpg",
                "image/jpeg", "test image content".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(storage.store(mockFile)).thenReturn(newImageName);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateImage(userId, mockFile);

        assertNotNull(result);
        verify(storage, never()).delete(anyString());
        verify(storage).store(mockFile);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateImagethrowNotFoundExceptionwhenUserNotFound() {
        Long userId = 999L;
        MultipartFile mockFile = new MockMultipartFile("file", "avatar.jpg",
                "image/jpeg", "test image content".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ProductException.NotFoundException.class, () -> authService.updateImage(userId, mockFile));

        verify(storage, never()).store(any());
        verify(storage, never()).delete(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByIdreturnUserwhenUserExists() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = authService.findById(userId);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void findByIdreturnNullwhenUserDoesNotExist() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User result = authService.findById(userId);

        assertNull(result);
        verify(userRepository).findById(userId);
    }

    @Test
    void findByEmailreturnUserwhenEmailExists() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        User result = authService.findByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmailthrowUsernameNotFoundExceptionwhenEmailDoesNotExist() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.findByEmail(email));

        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByUsernamereturnUserwhenUsernameExists() {
        String username = "testUser";
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(testUser));

        User result = authService.findByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository).findByUserName(username);
    }

    @Test
    void findByUsernamethrowUsernameNotFoundExceptionwhenUsernameDoesNotExist() {
        String username = "nonexistentUser";
        when(userRepository.findByUserName(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.findByUsername(username));

        verify(userRepository).findByUserName(username);
    }

    @Test
    void findAllreturnActiveUsersListwhenUsersExist() {
        List<User> users = List.of(testUser,
                User.builder().id(2L).userName("user2").build());

        when(userRepository.findAllActive()).thenReturn(users);

        List<User> result = authService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAllActive();
    }

    @Test
    void editsaveAndReturnUserwhenValidUserProvided() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = authService.edit(testUser);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteremoveUserFromDatabasewhenUserIdProvided() {
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        authService.delete(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteLogicalmarkUserAsDeletedwhenUserIdProvided() {
        Long userId = 1L;
        doNothing().when(userRepository).softDelete(userId);

        authService.deleteLogical(userId);

        verify(userRepository).softDelete(userId);
    }

    @Test
    void findByIdOptionalreturnActiveUserwhenUserExistsAndIsActive() {
        Long userId = 1L;
        when(userRepository.findActiveById(userId)).thenReturn(testUser);

        User result = authService.findByIdOptional(userId);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository).findActiveById(userId);
    }

    @Test
    void findByIdOptionalreturnNullwhenUserDoesNotExistOrIsDeleted() {
        Long userId = 999L;
        when(userRepository.findActiveById(userId)).thenReturn(null);

        User result = authService.findByIdOptional(userId);

        assertNull(result);
        verify(userRepository).findActiveById(userId);
    }

    @Test
    void softDeletemarkUserAsDeletedAndEvictCachewhenUserIdProvided() {
        Long userId = 1L;
        doNothing().when(userRepository).softDelete(userId);

        authService.softDelete(userId);

        verify(userRepository).softDelete(userId);
    }

    @Test
    void findAllPaginatedreturnAllUserswhenNoFilterProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(testUser);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = authService.findAllPaginated(Optional.empty(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void findAllPaginatedsearchByEmailwhenFilterContainsAtSymbol() {
        Pageable pageable = PageRequest.of(0, 10);
        String emailFilter = "test@example.com";
        List<User> users = List.of(testUser);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<User> result = authService.findAllPaginated(Optional.of(emailFilter), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userRepository, never()).findAll(pageable);
    }

    @Test
    void findAllPaginatedsearchByUsernamewhenFilterDoesNotContainAtSymbol() {
        Pageable pageable = PageRequest.of(0, 10);
        String usernameFilter = "testUser";
        List<User> users = List.of(testUser);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<User> result = authService.findAllPaginated(Optional.of(usernameFilter), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userRepository, never()).findAll(pageable);
    }
}
