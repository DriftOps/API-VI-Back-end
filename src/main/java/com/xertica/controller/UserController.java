package com.xertica.controller;

import com.xertica.dto.UserCreateDTO;
import com.xertica.dto.UserDTO;
import com.xertica.dto.UserLoginDTO;
import com.xertica.dto.UserViewDTO;
import com.xertica.entity.User;
import com.xertica.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDTO dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

        @PostMapping("/signup")
    public UserViewDTO signup(@RequestBody UserCreateDTO dto) {
        return userService.signup(dto);
    }

    @PostMapping("/login")
    public UserViewDTO login(@RequestBody UserLoginDTO dto) {
        return userService.login(dto);
    }
}