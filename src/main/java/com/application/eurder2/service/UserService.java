package com.application.eurder2.service;

import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateUserRequest;
import com.application.eurder2.dtos.request.RegisterUserRequest;
import com.application.eurder2.dtos.response.UserResponse;
import com.application.eurder2.enums.Role;
import com.application.eurder2.exception.ConflictException;
import com.application.eurder2.mapper.UserMapper;
import com.application.eurder2.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterUserRequest request) {
        return createUser(UserMapper.toEntity(request), Role.CUSTOMER);
    }

    public UserResponse createByAdmin(CreateUserRequest request) {
        return createUser(UserMapper.toEntity(request), request.role());
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse createUser(User user, Role role) {
        user.setRole(role);
        user.setEmail(normalizeEmail(user.getEmail()));
        if (repository.existsByEmail(user.getEmail())) throw ConflictException.emailAlreadyInUse();
        if (repository.existsByPhoneNumber(user.getPhoneNumber()))
            throw ConflictException.phoneNumberAlreadyInUse();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return UserMapper.toResponse(repository.save(user));
    }

    private boolean existsByEmail(String email) {return repository.existsByEmail(email);}
}