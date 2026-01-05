package com.example.ecomproj.auth.service.impl;

import com.example.ecomproj.Repository.UsersRepo;
import com.example.ecomproj.auth.service.UsersServices;
import com.example.ecomproj.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsersServicesImpl implements UsersServices {


    private UsersRepo userRepo;

    @Autowired
    public void setUsersRepo(UsersRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name()) // important: ROLE_ prefix
                .build();
    }
}
