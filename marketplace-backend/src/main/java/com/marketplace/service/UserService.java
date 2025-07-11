package com.marketplace.service;

import com.marketplace.dto.ChangePasswordRequest;
import com.marketplace.dto.UpdateProfileRequest;
import com.marketplace.model.User;
import com.marketplace.repository.UserRepository;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import com.marketplace.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.create(user);
    }


    public UserDetails loadUserById(String userId) {
        User user = getUserById(userId);
        return UserPrincipal.create(user);
    }

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BadRequestException("Email đã được sử dụng bởi tài khoản khác");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        user.setRole(user.getRole() != null ? user.getRole() : "USER");
        user.setEnabled(user.isEnabled());
        user.setVip(user.isVip());
        user.setVerified(user.isVerified());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);

        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhone(userDetails.getPhone());
        user.setAvatar(userDetails.getAvatar());
        user.setAddress(userDetails.getAddress());
        user.setBirthday(userDetails.getBirthday());
        user.setRole(userDetails.getRole());
        user.setEnabled(userDetails.isEnabled());
        user.setVip(userDetails.isVip());
        user.setVerified(userDetails.isVerified());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void toggleUserStatus(String id) {
        User user = getUserById(id);
        user.setEnabled(!user.isEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public boolean validatePassword(String email, String rawPassword) {
        User user = getUserByEmail(email);
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public User updateUserProfile(String userId, UpdateProfileRequest updateRequest) {
        User user = getUserById(userId);

        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        user.setPhone(updateRequest.getPhone());
        user.setBirthday(updateRequest.getBirthday());
        user.setAddress(updateRequest.getAddress());
        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar());
        }
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void deactivateUser(String userId) {
        User user = getUserById(userId);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void activateUser(String userId) {
        User user = getUserById(userId);
        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm);
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByEnabledTrue());
        stats.put("inactiveUsers", userRepository.countByEnabledFalse());
        stats.put("adminUsers", userRepository.countByRole("ADMIN"));
        stats.put("sellerUsers", userRepository.countByRole("SELLER"));
        stats.put("regularUsers", userRepository.countByRole("USER"));
        stats.put("vipUsers", userRepository.countByIsVipTrue());
        stats.put("verifiedUsers", userRepository.countByIsVerifiedTrue());

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newUsersThisMonth = userRepository.countByCreatedAtBetween(startOfMonth, LocalDateTime.now());
        stats.put("newUsersThisMonth", newUsersThisMonth);

        return stats;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ===== Saved Items =====
    public List<String> getSavedItems(String userId) {
        User user = getUserById(userId);
        return user.getSavedItems();
    }

    public void addSavedItem(String userId, String productId) {
        User user = getUserById(userId);
        if (!user.getSavedItems().contains(productId)) {
            user.getSavedItems().add(productId);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public void removeSavedItem(String userId, String productId) {
        User user = getUserById(userId);
        if (user.getSavedItems().remove(productId)) {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public void updateUserRole(String userId, String role) {
        User user = getUserById(userId);
        if (!role.equals("USER") && !role.equals("ADMIN") && !role.equals("SELLER")) {
            throw new BadRequestException("Vai trò không hợp lệ");
        }
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void toggleVipStatus(String userId) {
        User user = getUserById(userId);
        user.setVip(!user.isVip());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void deleteUser(String userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}