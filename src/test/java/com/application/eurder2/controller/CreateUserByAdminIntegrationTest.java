package com.application.eurder2.controller;

import com.application.eurder2.data.TestData;
import com.application.eurder2.data.UserJson;
import com.application.eurder2.domain.User;
import com.application.eurder2.enums.Role;
import com.application.eurder2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static com.application.eurder2.data.UserJson.basicAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for POST /users (admin only, Basic auth). */
@SpringBootTest
@Transactional
class CreateUserByAdminIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@eurder.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CUSTOMER_EMAIL = "customer@eurder.com";
    private static final String CUSTOMER_PASSWORD = "Customer1!";
    private static final String INVALID_CREDENTIALS = "These credentials are invalid.";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private String adminAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        saveUser(ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
        saveUser(CUSTOMER_EMAIL, CUSTOMER_PASSWORD, Role.CUSTOMER);
        adminAuth = basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    private void saveUser(String email, String rawPassword, Role role) {
        User user = TestData.newUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        repository.save(user);
    }

    private MockHttpServletRequestBuilder createRequest(String body) {
        return post("/users").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private ResultActions createAs(String authorization, String body) throws Exception {
        return mockMvc.perform(createRequest(body).header(HttpHeaders.AUTHORIZATION, authorization));
    }

    @Nested
    class Success {
        @ParameterizedTest
        @EnumSource(Role.class)
        void admin_canCreateEveryRole(Role role) throws Exception {
            UserJson json = UserJson.validWithRole(role.name());

            createAs(adminAuth, json.build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.email").value(json.email()))
                    .andExpect(jsonPath("$.role").value(role.name()));
        }

        @Test
        void responseNeverContainsPassword() throws Exception {
            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        void createdUser_hasHashedPassword() throws Exception {
            UserJson json = UserJson.validWithRole("ADMIN");

            createAs(adminAuth, json.build()).andExpect(status().isCreated());

            User saved = repository.getByEmail(json.email()).orElseThrow();
            assertThat(saved.getPassword()).isNotEqualTo("Password1!");
            assertThat(passwordEncoder.matches("Password1!", saved.getPassword())).isTrue();
        }

        @Test
        void createdAdmin_canThenCreateUsers() throws Exception {
            UserJson newAdmin = UserJson.validWithRole("ADMIN");
            createAs(adminAuth, newAdmin.build()).andExpect(status().isCreated());

            createAs(basicAuth(newAdmin.email(), "Password1!"), UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isCreated());
        }

        @Test
        void adminEmailInDifferentCase_isAccepted() throws Exception {
            createAs(basicAuth("ADMIN@Eurder.com", ADMIN_PASSWORD), UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    class Unauthorized {
        @Test
        void noAuthorizationHeader_returns401() throws Exception {
            mockMvc.perform(createRequest(UserJson.validWithRole("CUSTOMER").build()))
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void wrongPassword_returns401() throws Exception {
            createAs(basicAuth(ADMIN_EMAIL, "Wrong123!"), UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void unknownEmail_returns401_withSameMessage() throws Exception {
            createAs(basicAuth("ghost@eurder.com", ADMIN_PASSWORD), UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void passwordInDifferentCase_returns401() throws Exception {
            createAs(basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD.toLowerCase()), UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void hashAsPassword_returns401() throws Exception {
            String storedHash = repository.getByEmail(ADMIN_EMAIL).orElseThrow().getPassword();

            createAs(basicAuth(ADMIN_EMAIL, storedHash), UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isUnauthorized());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",                              // empty
                "Bearer abc.def.ghi",            // wrong scheme
                "Basic",                         // no token
                "Basic !!!not-base64!!!",        // invalid base64
                "Basic YWRtaW5AZXVyZGVyLmNvbQ==" // "admin@eurder.com" without ":password"
        })
        void malformedHeader_returns401(String header) throws Exception {
            createAs(header, UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void emptyPassword_returns401() throws Exception {
            createAs(basicAuth(ADMIN_EMAIL, ""), UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Forbidden {
        @Test
        void customer_returns403() throws Exception {
            createAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD), UserJson.validWithRole("CUSTOMER").build())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("You do not have the authorizations to go further."));
        }

        @Test
        void customer_cannotCreateAdmin_noUserSaved() throws Exception {
            UserJson json = UserJson.validWithRole("ADMIN");

            createAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD), json.build())
                    .andExpect(status().isForbidden());

            assertThat(repository.existsByEmail(json.email())).isFalse();
        }
    }

    @Nested
    class Conflict {
        @Test
        void existingEmail_returns409() throws Exception {
            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").with("email", CUSTOMER_EMAIL).build())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("This email is already in use."));
        }

        @Test
        void existingEmail_differentCase_returns409() throws Exception {
            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").with("email", "Customer@EURDER.com").build())
                    .andExpect(status().isConflict());
        }

        @Test
        void existingPhone_returns409() throws Exception {
            String takenPhone = repository.getByEmail(CUSTOMER_EMAIL).orElseThrow().getPhoneNumber();

            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").with("phoneNumber", takenPhone).build())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("This phone number is already in use."));
        }
    }

    @Nested
    class Validation {
        @Test
        void missingRole_returns400() throws Exception {
            createAs(adminAuth, UserJson.valid().build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.role").exists());
        }

        @ParameterizedTest
        @ValueSource(strings = {"SUPERADMIN", "admin", ""})
        void unknownRole_returns400(String role) throws Exception {
            createAs(adminAuth, UserJson.validWithRole(role).build())
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"firstname", "lastname", "email", "password", "phoneNumber", "address"})
        void missingField_returns400(String field) throws Exception {
            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").without(field).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + field).exists());
        }

        @Test
        void weakPassword_returns400() throws Exception {
            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").with("password", "weak").build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.password").exists());
        }

        @Test
        void invalidEmail_returns400() throws Exception {
            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").with("email", "not-an-email").build())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void invalidPhone_returns400() throws Exception {
            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").with("phoneNumber", "abc").build())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void blankAddressField_returns400() throws Exception {
            createAs(adminAuth, UserJson.validWithRole("CUSTOMER").withAddress("city", " ").build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$['address.city']").exists());
        }
    }
}