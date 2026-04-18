package com.example.handmademarket.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.handmademarket.dto.LoginRequest;
import com.example.handmademarket.dto.RegisterRequest;
import com.example.handmademarket.entity.Admin;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.AdminRepository;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.AuthService;
import com.example.handmademarket.util.JwtUtil;
import com.example.handmademarket.util.ResponseResult;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final String DEFAULT_PASSWORD = "a123456";

    public AuthServiceImpl(UserRepository userRepository, AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseResult login(LoginRequest request) {
        if (request.getUserAccount() == null || request.getUserAccount().trim().isEmpty()) {
            return ResponseResult.fail("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseResult.fail("密码不能为空");
        }

        Optional<User> userOptional = userRepository.findByUserAccount(request.getUserAccount());
        if (userOptional.isEmpty()) {
            return ResponseResult.fail("账号或密码错误");
        }
        User user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            Integer errorCount = user.getPwdErrorCount();
            if (errorCount == null) {
                errorCount = 1;
            } else {
                errorCount++;
            }
            user.setPwdErrorCount(errorCount);

            if (errorCount >= 5) {
                user.setStatus(2);
                user.setLockTime(LocalDateTime.now());
            }
            userRepository.save(user);
            return ResponseResult.fail("密码错误，剩余" + (5 - errorCount) + "次机会");
        }

        // 登录成功，重置错误计数
        user.setPwdErrorCount(0);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUserAccount());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userAccount", user.getUserAccount());
        data.put("userName", user.getUserName());
        data.put("role", user.getRole());
        data.put("userId", user.getUser_id());

        return ResponseResult.ok("登录成功", data);
    }

    @Override
    public ResponseResult register(RegisterRequest request) {
        if (request.getUserAccount() == null || request.getUserAccount().trim().isEmpty()) {
            return ResponseResult.fail("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseResult.fail("密码不能为空");
        }

        if (userRepository.existsByUserAccount(request.getUserAccount())) {
            return ResponseResult.fail("用户名已存在");
        }

        User user = new User();
        user.setUserAccount(request.getUserAccount());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "1");
        user.setRegisterTime(LocalDateTime.now());
        user.setStatus(1);
        user.setCreditScore(80);

        userRepository.save(user);

        return ResponseResult.ok("注册成功");
    }

    @Override
    public ResponseResult resetPassword(String account, String code) {
        Optional<User> userOptional = userRepository.findByUserAccount(account);
        if (userOptional.isEmpty()) {
            return ResponseResult.fail("账号不存在");
        }
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setPwdErrorCount(0);
        user.setStatus(1);
        userRepository.save(user);
        return ResponseResult.ok("密码已重置为默认密码：" + DEFAULT_PASSWORD);
    }
}