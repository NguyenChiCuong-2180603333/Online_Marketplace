package com.marketplace.service;

import com.marketplace.dto.JwtResponse;
import com.marketplace.dto.LoginRequest;
import com.marketplace.dto.RegisterRequest;
import com.marketplace.model.User;
import com.marketplace.security.JwtTokenProvider;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public User register(RegisterRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());

        return userService.createUser(user);
    }

    public JwtResponse login(LoginRequest loginRequest) {
        if (!userService.validatePassword(loginRequest.getEmail(), loginRequest.getPassword())) {
            throw new BadRequestException("Email hoặc mật khẩu không đúng");
        }

        User user = userService.getUserByEmail(loginRequest.getEmail());

        if (!user.isEnabled()) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        String token = jwtTokenProvider.generateToken(user.getId());

        return new JwtResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}