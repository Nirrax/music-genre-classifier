package com.sa.spring_api.user.service;

import com.sa.spring_api.auth.dto.RegisterRequest;
import com.sa.spring_api.user.dto.UpdateUserRequest;
import com.sa.spring_api.user.exception.UserNotFoundException;
import com.sa.spring_api.user.model.User;
import com.sa.spring_api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return this.userRepository.findById(id).orElseThrow( () -> new UserNotFoundException("User not found"));
    }

    @Transactional
    public User updateUserPasswordById(UUID id, UpdateUserRequest credentials) {
        if (credentials.currentPassword().equals(credentials.newPassword())) {
            throw new IllegalArgumentException("Passwords do not differ from each other");
        }

        User user = getUserById(id);
        if (!passwordEncoder.matches(credentials.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(credentials.newPassword()));
        return this.userRepository.save(user);
    }

    @Transactional
    public void deleteUserById(UUID id) {
        this.userRepository.findById(id).ifPresent(userRepository::delete);
    }

    @Transactional
    public User createUser(RegisterRequest user) {
        return userRepository.save(new User(user.username(), user.password()));
    }

}
