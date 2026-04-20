package com.example.handmademarket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.handmademarket.dto.LoginRequest;
import com.example.handmademarket.dto.PasswordResetRequest;
import com.example.handmademarket.dto.RegisterRequest;
import com.example.handmademarket.service.AuthService;
import com.example.handmademarket.util.ResponseResult;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseResult> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseResult> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<ResponseResult> resetPassword(@RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request.getAccount(), "123456"));
    }
}