package com.marketplace.controller;

import com.marketplace.dto.LoginRequest;
import com.marketplace.dto.RegisterRequest;
import com.marketplace.model.User;
import com.marketplace.service.AuthService;
import com.marketplace.service.MarketplaceEventListener;
import com.marketplace.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private MarketplaceEventListener eventListener;
    
    @Autowired
    private UserService userService;


    @PostMapping("/register")
public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
    try {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Email đã được sử dụng");
            response.put("field", "email");
            return ResponseEntity.badRequest().body(response);
        }

        User user = authService.register(registerRequest);

        eventListener.handleUserRegistration(
                user.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng ký thành công");
        response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName()
        ));

        return ResponseEntity.ok(response);
        
    } catch (MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        e.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });
        
        response.put("error", "Dữ liệu không hợp lệ");
        response.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(response);
        
    } catch (DataIntegrityViolationException e) {
        Map<String, Object> response = new HashMap<>();
        if (e.getMessage().contains("email")) {
            response.put("error", "Email đã được sử dụng");
            response.put("field", "email");
        } else {
            response.put("error", "Dữ liệu bị trùng lặp");
        }
        return ResponseEntity.badRequest().body(response);
        
    } catch (Exception e) {
        System.err.println("Registration error: " + e.getMessage());
        e.printStackTrace();
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Đăng ký thất bại. Vui lòng thử lại.");
        response.put("details", e.getMessage()); 
        return ResponseEntity.status(500).body(response);
    }
}

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Map<String, Object> authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng xuất thành công");
        return ResponseEntity.ok(response);
    }
}