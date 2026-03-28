package com.sa.spring_api.user.service;

import com.sa.spring_api.user.dto.UpdateUserRequest;
import com.sa.spring_api.user.exception.UserNotFoundException;
import com.sa.spring_api.user.model.User;
import com.sa.spring_api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void getUsers_whenNoUserExist_returnsEmptyList() {
        List<User> users = new ArrayList<>();
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getUsers();

        assertThat(result)
                .isNotNull()
                .hasSize(0)
                .isEmpty();

        verify(userRepository).findAll();
    }

    @Test
    public void getUsers_whenUsersExist_returnsUsersList() {
        List<User> users = List.of(new User(), new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getUsers();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(users.size())
                .containsExactlyElementsOf(users);


        verify(userRepository).findAll();
    }

    @Test
    public void getUserById_whenUserExist_returnsUser() {
        UUID userId = UUID.randomUUID();
        User user = new User("John Doe", "password");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(user.getUsername()).isEqualTo(result.getUsername());
        assertThat(user.getPasswordHash()).isEqualTo(result.getPasswordHash());

        verify(userRepository).findById(userId);
    }

    @Test
    public void getUserById_whenUserDoesNotExist_throwsUserNotFoundException() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(nonExistentUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(nonExistentUserId);
    }

    @Test
    public void updateUserPasswordWithId_whenPasswordsDoesNotDiffer_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest("oldPassword", "oldPassword");

        assertThatThrownBy(() -> userService.updateUserPasswordById(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not differ from each other");

        verify(userRepository, never()).save(any());
    }

    @Test
    public void updateUserPasswordWithId_whenOldPasswordDoesNotMatch_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest("wrongPassword", "newPassword");
        User mockUser = new User();
        mockUser.setPasswordHash("hashedCurrentPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", "hashedCurrentPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.updateUserPasswordById(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Old password does not match");

        verify(passwordEncoder).matches("wrongPassword", "hashedCurrentPassword");
        verify(userRepository, never()).save(any());
    }

    @Test
    public void deleteUserById_whenUserDoesNotExist_doNothing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        userService.deleteUserById(userId);

        verify(userRepository, never()).delete(any());
    }

    @Test
    public void deleteUserById_whenUserExist_deletesUser() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        userService.deleteUserById(userId);

        verify(userRepository).delete(mockUser);
    }
}


