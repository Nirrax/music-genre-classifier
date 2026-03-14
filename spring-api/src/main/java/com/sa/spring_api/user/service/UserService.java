package com.sa.spring_api.user.service;

import com.sa.spring_api.auth.dto.RegisterRequest;
import com.sa.spring_api.user.dto.UpdateUserRequest;
import com.sa.spring_api.user.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> getUsers();

    User getUserById(UUID id);

    User updateUserPasswordById(UUID id, UpdateUserRequest credentials);

    void deleteUserById(UUID id);

    User createUser(RegisterRequest dto);
}
