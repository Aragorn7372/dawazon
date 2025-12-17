package dev.luisvives.dawazon.users.service;

import dev.luisvives.dawazon.users.dto.UserAdminRequestDto;
import dev.luisvives.dawazon.users.dto.UserChangePasswordDto;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.models.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface AuthService {
    User register(User u);
    User findById(long id);
    User findByEmail(String email);
    User findByUsername(String userName);
    List<User> findAll();
    User edit(User u);
    void delete(Long id);
    User findByIdOptional(Long id);
    void softDelete(Long id);
    Page<User> findAllPaginated(Optional<String> userNameOrEmail,Pageable pageable);
    User changePassword(UserChangePasswordDto userDto, Long id);

    User updateCurrentUser(Long id, @Valid UserRequestDto updateUser);
    User updateAdminCurrentUser(Long id, UserAdminRequestDto updateUser);
    User updateImage(Long id,MultipartFile file);
}
