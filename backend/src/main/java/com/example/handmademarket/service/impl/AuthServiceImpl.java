package com.example.handmademarket.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.handmademarket.dto.LoginRequest;
import com.example.handmademarket.dto.RegisterRequest;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.AuthService;
import com.example.handmademarket.util.JwtUtil;
import com.example.handmademarket.util.ResponseResult;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseResult login(LoginRequest request) {
        // 验证输入
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseResult.fail("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseResult.fail("密码不能为空");
        }

        // 查找用户
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isEmpty()) {
            return ResponseResult.fail("用户名或密码错误");
        }

        User user = userOptional.get();

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseResult.fail("用户名或密码错误");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        // 返回token
        return ResponseResult.ok(token);
    }

    @Override
    public ResponseResult register(RegisterRequest request) {
        // 验证输入
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseResult.fail("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseResult.fail("密码不能为空");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseResult.fail("邮箱不能为空");
        }
        // 简单邮箱格式验证
        if (!request.getEmail().contains("@")) {
            return ResponseResult.fail("邮箱格式不正确");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseResult.fail("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : "consumer"); // 默认角色

        // 保存用户
        userRepository.save(user);

        return ResponseResult.ok("注册成功");
    }
}
