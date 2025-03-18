package com.project2.ShoppingWeb.Security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Repository.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Optional<User> user = userRepo.findByName(username);
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }

        return AuthUser.builder()
                .user(user.get())
                .build();
    }

}
