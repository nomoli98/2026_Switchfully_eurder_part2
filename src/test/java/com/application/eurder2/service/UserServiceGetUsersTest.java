package com.application.eurder2.service;

import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.response.UserResponse;
import com.application.eurder2.enums.Role;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for UserService.getUsers and UserService.getUserById. */
@ExtendWith(MockitoExtension.class)
class UserServiceGetUsersTest {

    private final Pageable pageable = PageRequest.of(0, 50);

    @Mock
    private UserRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void getUsers_noRole_callsFindAll() {
        User user = TestData.newUser();
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));

        List<UserResponse> result = userService.getUsers(null, pageable);

        assertThat(result).extracting(UserResponse::email).containsExactly(user.getEmail());
        verify(repository, never()).findAllByRole(any(), any());
    }

    @Test
    void getUsers_withRole_callsFindAllByRole() {
        when(repository.findAllByRole(Role.ADMIN, pageable)).thenReturn(new PageImpl<>(List.of()));

        userService.getUsers(Role.ADMIN, pageable);

        verify(repository).findAllByRole(Role.ADMIN, pageable);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getUsers_noUsers_returnsEmptyList() {
        when(repository.findAll(pageable)).thenReturn(Page.empty());

        assertThat(userService.getUsers(null, pageable)).isEmpty();
    }

    @Test
    void getUserById_found_returnsMappedUser() {
        UUID id = UUID.randomUUID();
        User user = TestData.newUser();
        when(repository.findById(id)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(id);

        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.role()).isEqualTo(user.getRole());
        assertThat(result.address().city()).isEqualTo("Brussels");
    }

    @Test
    void getUserById_notFound_throwsExistenceException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(ExistenceException.class)
                .hasMessage("This user is not found in the database.");
    }
}