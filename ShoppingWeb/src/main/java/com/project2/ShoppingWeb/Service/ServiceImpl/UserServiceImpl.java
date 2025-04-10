package com.project2.ShoppingWeb.Service.ServiceImpl;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project2.ShoppingWeb.DTO.LoginRequest;
import com.project2.ShoppingWeb.DTO.LoginResponse;
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Service.UserService;

import jakarta.annotation.PostConstruct;

import com.project2.ShoppingWeb.Enums.UserRole;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import com.project2.ShoppingWeb.Security.JwtUtils;
import com.project2.ShoppingWeb.Repository.UserRepo;
import com.project2.ShoppingWeb.DTO.ChangePasswordRequest;


import com.project2.ShoppingWeb.Enums.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    @PostConstruct
    public void createAdminAccount() {
        try {
            // Kiểm tra xem admin đã tồn tại chưa
            if (!userRepo.findByEmail("admin@example.com").isPresent()) {
                User admin = User.builder()
                        .name("admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))  // Tự động tạo hash mới
                        .role(UserRole.ADMIN)
                        .phoneNumber("0123456789")
                        .build();
                
                userRepo.save(admin);
                log.info("Admin account created successfully");
            }
        } catch (Exception e) {
            log.error("Error creating admin account: ", e);
        }
    }

    

    @Override
    public User registerUser(User registrationRequest) {
        // Kiểm tra email tồn tại
        if (userRepo.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Validate input
        if (registrationRequest.getEmail() == null || !registrationRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Email không hợp lệ");
        }
        
        if (registrationRequest.getPassword() == null || registrationRequest.getPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (registrationRequest.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Không thể đăng ký tài khoản admin");
        }

        UserRole role = UserRole.USER;

        try {
            User user = User.builder()
                    .name(registrationRequest.getName())
                    .email(registrationRequest.getEmail())
                    .password(passwordEncoder.encode(registrationRequest.getPassword()))
                    .phoneNumber(registrationRequest.getPhoneNumber())
                    .role(role)
                    .build();
            
            return userRepo.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đăng ký người dùng: " + e.getMessage());
        }
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        try {
            log.info("Attempting to login with email: {}", loginRequest.getEmail());
            
            User user = userRepo.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new NotFoundException("Email không tồn tại trong hệ thống"));
            log.info("User found: {}", user.getEmail());
            
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.error("Password does not match for user: {}", user.getEmail());
                throw new RuntimeException("Mật khẩu không chính xác");
            }
            log.info("Password matched successfully");
            
            String token = jwtUtils.generateToken(user);
            log.info("JWT token generated successfully");
            
            return LoginResponse.builder()
                    .email(user.getEmail())
                    .token(token)
                    .role(user.getRole())
                    .build();
        } catch (Exception e) {
            log.error("Login error: ", e);
            throw new RuntimeException("Lỗi khi đăng nhập: " + e.getMessage());
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Người dùng chưa đăng nhập");
        }

        String email = authentication.getName(); // Lấy email từ authentication
        
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy thông tin người dùng"));
    }

    @Override
    public User getUserInfoAndOrderHistory() {
        try {
            User user = getLoginUser();
            return user;  // Trả về user info mà không cần kiểm tra thêm
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user: ", e);
            throw new RuntimeException("Không thể lấy thông tin người dùng");
        }
    }

    @Override
    public User getUserById(Integer id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
    }

}
