package com.application.eurder2.controller;

import com.application.eurder2.data.UserJson;
import com.application.eurder2.domain.User;
import com.application.eurder2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RegisterUserIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private ResultActions register(String body) throws Exception {
        return mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Nested
    class Success {
        @Test
        void validRequest_returns201WithUser() throws Exception {
            UserJson json = UserJson.valid();

            register(json.build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.firstname").value("Nora"))
                    .andExpect(jsonPath("$.lastname").value("Petry"))
                    .andExpect(jsonPath("$.email").value(json.email()))
                    .andExpect(jsonPath("$.phoneNumber").value(json.phone()))
                    .andExpect(jsonPath("$.address.city").value("Brussels"))
                    .andExpect(jsonPath("$.role").value("CUSTOMER"));
        }

        @Test
        void responseNeverContainsPassword() throws Exception {
            register(UserJson.valid().build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        void userIsPersistedWithHashedPassword() throws Exception {
            UserJson json = UserJson.valid();

            register(json.build()).andExpect(status().isCreated());

            User saved = repository.getByEmail(json.email()).orElseThrow();
            assertThat(saved.getPassword()).isNotEqualTo("Password1!").startsWith("$2");
            assertThat(passwordEncoder.matches("Password1!", saved.getPassword())).isTrue();
        }

        @Test
        void roleInBody_isIgnored_alwaysCustomer() throws Exception {
            register(UserJson.valid().with("role", "ADMIN").build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("CUSTOMER"));
        }

        @Test
        void email_isStoredLowercase() throws Exception {
            register(UserJson.valid().with("email", "Nora.UPPER@Test.COM").build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("nora.upper@test.com"));

            assertThat(repository.existsByEmail("nora.upper@test.com")).isTrue();
        }

        @Test
        void phoneWithoutPlus_isAccepted() throws Exception {
            register(UserJson.valid().with("phoneNumber", "0470123456").build())
                    .andExpect(status().isCreated());
        }

        @Test
        void passwordWithExactly8Chars_isAccepted() throws Exception {
            register(UserJson.valid().with("password", "Abcdef1!").build())
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    class Conflict {
        @Test
        void duplicateEmail_returns409() throws Exception {
            UserJson first = UserJson.valid();
            register(first.build()).andExpect(status().isCreated());

            register(UserJson.valid().with("email", first.email()).build())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("This email is already in use."));
        }

        @Test
        void duplicateEmail_differentCase_returns409() throws Exception {
            register(UserJson.valid().with("email", "case@test.com").build()).andExpect(status().isCreated());

            register(UserJson.valid().with("email", "CASE@Test.com").build())
                    .andExpect(status().isConflict());
        }

        @Test
        void duplicatePhone_returns409() throws Exception {
            UserJson first = UserJson.valid();
            register(first.build()).andExpect(status().isCreated());

            register(UserJson.valid().with("phoneNumber", first.phone()).build())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("This phone number is already in use."));
        }

        @Test
        void conflict_doesNotCreateSecondUser() throws Exception {
            UserJson first = UserJson.valid();
            register(first.build());
            long count = repository.count();

            register(UserJson.valid().with("email", first.email()).build());

            assertThat(repository.count()).isEqualTo(count);
        }
    }

    @Nested
    class Validation {
        @ParameterizedTest
        @ValueSource(strings = {"firstname", "lastname", "email", "password", "phoneNumber", "address"})
        void missingField_returns400(String field) throws Exception {
            register(UserJson.valid().without(field).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + field).exists());
        }

        @ParameterizedTest
        @ValueSource(strings = {"firstname", "lastname", "email", "password", "phoneNumber"})
        void blankField_returns400(String field) throws Exception {
            register(UserJson.valid().with(field, "   ").build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + field).exists());
        }

        @ParameterizedTest
        @ValueSource(strings = {"not-an-email", "nora@", "@test.com", "nora test@test.com"})
        void invalidEmail_returns400(String email) throws Exception {
            register(UserJson.valid().with("email", email).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.email").exists());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Abcde1!",      // 7 chars
                "Password!",    // no digit
                "password1!",   // no uppercase
                "Password1"     // no special char
        })
        void weakPassword_returns400(String password) throws Exception {
            register(UserJson.valid().with("password", password).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.password")
                                       .value("Password needs 8+ characters, a digit, an uppercase letter and a special character"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "12345678",       // 8 digits, too short
                "+321234567890",  // 12 digits, too long
                "04701234ab",     // letters
                "0470 12 34 56",  // spaces
                "++32470123456"   // double plus
        })
        void invalidPhone_returns400(String phone) throws Exception {
            register(UserJson.valid().with("phoneNumber", phone).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.phoneNumber").value("Invalid phone number"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"street", "number", "city", "postalCode", "country"})
        void blankAddressField_returns400(String field) throws Exception {
            register(UserJson.valid().withAddress(field, "").build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$['address." + field + "']").exists());
        }

        @Test
        void invalidRequest_doesNotCreateUser() throws Exception {
            long count = repository.count();

            register(UserJson.valid().with("password", "weak").build());

            assertThat(repository.count()).isEqualTo(count);
        }

        @Test
        void malformedJson_returns400() throws Exception {
            register("{ \"firstname\": ").andExpect(status().isBadRequest());
        }

        @Test
        void emptyBody_returns400() throws Exception {
            register("").andExpect(status().isBadRequest());
        }
    }
}