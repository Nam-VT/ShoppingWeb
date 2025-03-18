package com.project2.ShoppingWeb.Service.ServiceImpl;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project2.ShoppingWeb.DTO.LoginRequest;
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Service.UserService;
import com.project2.ShoppingWeb.Enums.UserRole;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import com.project2.ShoppingWeb.Security.JwtUtils;
import com.project2.ShoppingWeb.Repository.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public User registerUser(User registrationRequest) {
        // TODO Auto-generated method stub
        UserRole role = UserRole.USER;

        User user = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()) )
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .build();
        
        User savedUser = userRepo.save(user);        

        return savedUser;
    }

    @Override
    public LoginRequest loginUser(LoginRequest loginRequest) {
        // TODO Auto-generated method stub
        User user = userRepo.findByName(loginRequest.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(user == null) {
            throw new RuntimeException("User not found");
        };

        String token = null;
        if(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            token = jwtUtils.generateToken(user);
            log.info("Token generated: " + token);
        } else {
            throw new RuntimeException("Invalid password");
        }

        return LoginRequest.builder()
                .name(user.getName())
                .token(token)
                .build();
    }

    @Override
    public List<User> getAllUsers() {
        // TODO Auto-generated method stub
        return userRepo.findAll();
    }

    @Override
    public User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String  name = authentication.getName();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("No authenticated user found");
        }
        else{
            log.info("User name is: " + name);
        }    
        return userRepo.findByName(name)
                .orElseThrow(()-> new UsernameNotFoundException("User Not found"));
    }

    @Override
    public User getUserInfoAndOrderHistory() {
        User user = getLoginUser();

        if (user.getAddress() == null) {
            throw new NotFoundException("Người dùng chưa thêm địa chỉ.");
        }

        if (user.getOrderItemlist() == null || user.getOrderItemlist().isEmpty()) {
            throw new NotFoundException("Người dùng chưa có lịch sử đặt hàng.");
        }

        return user;
    }

    @Override
    public User getUserById(Integer id) {
        // TODO Auto-generated method stub
        return userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
