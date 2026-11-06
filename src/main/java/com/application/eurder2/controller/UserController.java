package com.application.eurder2.controller;

import com.application.eurder2.dtos.request.CreateUserRequest;
import com.application.eurder2.dtos.request.RegisterUserRequest;
import com.application.eurder2.dtos.response.UserResponse;
import com.application.eurder2.enums.Permission;
import com.application.eurder2.enums.Role;
import com.application.eurder2.security.SecurityService;
import com.application.eurder2.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;
    private final SecurityService securityService;

    public UserController(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
    }

    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Get all users")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            @Parameter(hidden = true) String authorization,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
        securityService.validateAuthorization(authorization, Permission.VIEW_USERS);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(direction, sortBy));
        return userService.getUsers(role, pageable);
    }

    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Get a user by his id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401",
                         description = "Missing or invalid Authorization header"),
            @ApiResponse(responseCode = "403", description = "Not allowed to view users"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping(path = "{id}")
    @ResponseStatus(HttpStatus.FOUND)
    public UserResponse getUserById(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            @Parameter(hidden = true) String authorization,
            @PathVariable("id") UUID userId) {
        securityService.validateAuthorization(authorization, Permission.VIEW_USERS);
        return userService.getUserById(userId);
    }

    @Operation(summary = "Register as a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return userService.register(request);
    }

    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Create a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid user"),
            @ApiResponse(responseCode = "401",
                         description = "Missing or invalid Authorization header"),
            @ApiResponse(responseCode = "403", description = "Not allowed to create users"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createByAdmin(@RequestHeader(value = HttpHeaders.AUTHORIZATION,
                                                     required = false) @Parameter(hidden = true)
                                      String authorization,
                                      @Valid @RequestBody CreateUserRequest request) {
        securityService.validateAuthorization(authorization, Permission.CREATE_USER);
        return userService.createByAdmin(request);
    }
}