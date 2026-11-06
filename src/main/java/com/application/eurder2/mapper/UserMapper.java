package com.application.eurder2.mapper;

import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateUserRequest;
import com.application.eurder2.dtos.request.RegisterUserRequest;
import com.application.eurder2.dtos.response.UserResponse;

public class UserMapper {
    public static User toEntity(RegisterUserRequest request) {
        return new User(
                request.firstname(),
                request.lastname(),
                request.email(),
                request.password(),
                request.phoneNumber(),
                AddressMapper.toEntity(request.address()));
    }

    public static User toEntity(CreateUserRequest request) {
        return new User(
                request.firstname(),
                request.lastname(),
                request.email(),
                request.password(),
                request.phoneNumber(),
                AddressMapper.toEntity(request.address()));
    }

    public static UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPhoneNumber(),
                AddressMapper.toResponse(user.getAddress()),
                user.getRole());
    }
}
