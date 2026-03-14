package com.sa.spring_api.user.controller;

import com.sa.spring_api.config.security.JwtUserDetails;
import com.sa.spring_api.user.dto.UpdateUserRequest;
import com.sa.spring_api.user.dto.UserDTO;
import com.sa.spring_api.user.model.User;
import com.sa.spring_api.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getLoggedInUser(@AuthenticationPrincipal JwtUserDetails user) {
        User userInfo = userService.getUserById(user.getId());

        return new ResponseEntity<>(UserDTO.from(userInfo), HttpStatus.OK);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDTO> updateLoggedInUser(@RequestBody @Valid UpdateUserRequest credentials,
                                      @AuthenticationPrincipal JwtUserDetails user) {
        User userInfo = userService.updateUserPasswordById(user.getId(), credentials);

        return new ResponseEntity<>(UserDTO.from(userInfo), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteLoggedInUser(@AuthenticationPrincipal JwtUserDetails user) {
        userService.deleteUserById(user.getId());

        return new ResponseEntity<>(new Object(), HttpStatus.OK);
    }
}
