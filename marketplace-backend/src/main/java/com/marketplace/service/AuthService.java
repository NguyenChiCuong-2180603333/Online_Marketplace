package com.marketplace.service;

import com.marketplace.dto.LoginRequest;
import com.marketplace.dto.RegisterRequest;
import com.marketplace.model.User;
import com.marketplace.repository.UserRepository;
import com.marketplace.security.JwtTokenProvider;
import com.marketplace.security.UserPrincipal;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public User register(RegisterRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());

        return userService.createUser(user);
    }

    public Map<String, Object> login(LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail().trim().toLowerCase();
            String password = loginRequest.getPassword();

            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
                throw new BadRequestException("Tài khoản không tồn tại");
            }

            User user = optionalUser.get();

            if (!user.isEnabled()) {
                throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng liên hệ hỗ trợ");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadRequestException("Mật khẩu không đúng");
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    email, 
                    password
            );
            
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, 
                    password, 
                    userPrincipal.getAuthorities()
            );
            
            String jwt = jwtTokenProvider.generateToken(authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole(),
                    "avatar", user.getAvatar() != null ? user.getAvatar() : ""
            ));
            response.put("message", "Đăng nhập thành công");

            return response;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected login error: " + e.getMessage());
            e.printStackTrace();
            throw new BadRequestException("Đăng nhập thất bại. Vui lòng thử lại");
        }
    }
}