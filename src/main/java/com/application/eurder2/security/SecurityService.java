package com.application.eurder2.service;

import com.application.eurder2.domain.User;
import com.application.eurder2.enums.Permission;
import com.application.eurder2.exception.ForbiddenException;
import com.application.eurder2.exception.UnauthorizedException;
import com.application.eurder2.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SecurityService {
    private static final String BASIC_PREFIX = "Basic ";

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public SecurityService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User validateAuthorization(String authorizationHeader, Permission permissionNeeded) {
        User user = authenticate(authorizationHeader);
        if (!user.hasPermission(permissionNeeded)) throw ForbiddenException.invalidPermission();
        return user;
    }

    public User authenticate(String authorizationHeader) {
        Credentials credentials = decode(authorizationHeader);
        User user = repository.getByEmail(UserService.normalizeEmail(credentials.email()))
                              .orElseThrow(UnauthorizedException::invalidCredentials);
        if (!verifyPassword(credentials.password(), user.getPassword())) {
            throw UnauthorizedException.invalidCredentials();
        }
        return user;
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) return false;
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    private Credentials decode(String header) {
        if (header == null || !header.startsWith(BASIC_PREFIX))
            throw UnauthorizedException.invalidCredentials();
        String decoded;
        try {
            decoded = new String(Base64.getDecoder()
                                       .decode(header.substring(BASIC_PREFIX.length()).trim()),
                                 StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw UnauthorizedException.invalidCredentials();
        }
        int separator = decoded.indexOf(':');
        if (separator <= 0) throw UnauthorizedException.invalidCredentials();
        return new Credentials(decoded.substring(0, separator), decoded.substring(separator + 1));
    }

    private record Credentials(String email, String password) {
    }
}
