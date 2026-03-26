package com.jordi.kakebot.user.controller;

import com.jordi.kakebot.user.dto.UserLoginRequestDto;
import com.jordi.kakebot.user.dto.UserRegisterRequestDto;
import com.jordi.kakebot.user.dto.UserResponseDto;
import com.jordi.kakebot.user.dto.UserUpdateRequestDto;
import com.jordi.kakebot.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterRequestDto request) {
        UserResponseDto response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody UserLoginRequestDto request) {
        UserResponseDto response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@RequestHeader("X-User-Id") Long userId) {
        UserResponseDto response = userService.getMe(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMe(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        UserResponseDto response = userService.updateMe(userId, request);
        return ResponseEntity.ok(response);
    }
}
