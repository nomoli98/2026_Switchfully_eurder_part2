package com.application.eurder2.service;

import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateAddressRequest;
import com.application.eurder2.dtos.request.CreateUserRequest;
import com.application.eurder2.dtos.request.RegisterUserRequest;
import com.application.eurder2.enums.Role;
import com.application.eurder2.exception.ConflictException;
import com.application.eurder2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Unit tests: UserService must hash the password before saving. */
@ExtendWith(MockitoExtension.class)
class UserServicePasswordHashingTest {

    private static final String RAW = "Password1!";
    private static final String HASH = "$2a$10$hashedValue";

    @Mock
    private UserRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private final CreateAddressRequest address = new CreateAddressRequest("Main Street", "12A", "Brussels", "1000", "Belgium");

    private RegisterUserRequest registerRequest() {
        return new RegisterUserRequest("Nora", "Petry", "nora@test.com", RAW, "+32470000001", address);
    }

    private CreateUserRequest createRequest() {
        return new CreateUserRequest("Nora", "Petry", "nora@test.com", RAW, "+32470000001", address, Role.ADMIN);
    }

    private User captureSavedUser() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private void stubHappyPath() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(RAW)).thenReturn(HASH);
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void register_savesHashedPassword_notRawPassword() {
        stubHappyPath();

        userService.register(registerRequest());

        verify(passwordEncoder).encode(RAW);
        assertThat(captureSavedUser().getPassword()).isEqualTo(HASH).isNotEqualTo(RAW);
    }

    @Test
    void createByAdmin_savesHashedPassword_notRawPassword() {
        stubHappyPath();

        userService.createByAdmin(createRequest());

        verify(passwordEncoder).encode(RAW);
        assertThat(captureSavedUser().getPassword()).isEqualTo(HASH);
    }

    @Test
    void register_encodesPasswordExactlyOnce() {
        stubHappyPath();

        userService.register(registerRequest());

        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void duplicateEmail_doesNotHashNorSave() {
        when(repository.existsByEmail("nora@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest())).isInstanceOf(ConflictException.class);

        verifyNoInteractions(passwordEncoder);
        verify(repository, never()).save(any());
    }

    @Test
    void duplicatePhone_doesNotHashNorSave() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByPhoneNumber("+32470000001")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest())).isInstanceOf(ConflictException.class);

        verifyNoInteractions(passwordEncoder);
        verify(repository, never()).save(any());
    }
}