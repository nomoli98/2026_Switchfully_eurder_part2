package com.application.eurder2.service;

import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateUserRequest;
import com.application.eurder2.dtos.request.RegisterUserRequest;
import com.application.eurder2.dtos.response.UserResponse;
import com.application.eurder2.enums.Role;
import com.application.eurder2.exception.ConflictException;
import com.application.eurder2.mapper.UserMapper;
import com.application.eurder2.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserResponse register(RegisterUserRequest request) {
        return createUser(UserMapper.toEntity(request), Role.CUSTOMER);
    }

    public UserResponse createByAdmin(CreateUserRequest request) {
        return createUser(UserMapper.toEntity(request), request.role());
    }

    private UserResponse createUser(User user, Role role) {
        user.setRole(role);
        if(repository.existsByEmail(user.getEmail())) throw ConflictException.emailAlreadyInUse();
        
        return UserMapper.toResponse(repository.save(user));
    }
}
