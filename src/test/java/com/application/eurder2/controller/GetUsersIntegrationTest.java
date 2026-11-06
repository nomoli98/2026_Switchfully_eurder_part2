package com.application.eurder2.controller;

import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.User;
import com.application.eurder2.enums.Role;
import com.application.eurder2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static com.application.eurder2.data.UserJson.basicAuth;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for GET /users and GET /users/{id} (needs VIEW_USERS, Basic auth). */
@SpringBootTest
@Transactional
class GetUsersIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@eurder.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CUSTOMER_EMAIL = "customer@eurder.com";
    private static final String CUSTOMER_PASSWORD = "Customer1!";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private String adminAuth;
    private User admin;
    private User customer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        admin = saveUser(ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN, "Zeta");
        customer = saveUser(CUSTOMER_EMAIL, CUSTOMER_PASSWORD, Role.CUSTOMER, "Alpha");
        adminAuth = basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    private User saveUser(String email, String rawPassword, Role role, String lastname) {
        User user = TestData.newUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setLastname(lastname);
        return repository.save(user);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, String authorization) throws Exception {
        return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, authorization));
    }

    @Nested
    class GetAllUsers {

        @Test
        void admin_getsAllUsers() throws Exception {
            perform(get("/users"), adminAuth)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        void defaultSort_isLastNameAscending() throws Exception {
            perform(get("/users"), adminAuth)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].lastname").value("Alpha"))
                    .andExpect(jsonPath("$[1].lastname").value("Zeta"));
        }

        @Test
        void sortDesc_reversesOrder() throws Exception {
            perform(get("/users").param("direction", "DESC"), adminAuth)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].lastname").value("Zeta"));
        }

        @Test
        void filterByRole_returnsOnlyThatRole() throws Exception {
            perform(get("/users").param("role", "CUSTOMER"), adminAuth)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[*].role", everyItem(is("CUSTOMER"))));
        }

        @Test
        void limit_capsNumberOfResults() throws Exception {
            perform(get("/users").param("limit", "1"), adminAuth)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        void responseNeverContainsPassword() throws Exception {
            perform(get("/users"), adminAuth)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].password").doesNotExist());
        }

        @Test
        void unknownRole_returns400() throws Exception {
            perform(get("/users").param("role", "SUPERADMIN"), adminAuth)
                    .andExpect(status().isBadRequest());
        }

        @Test
        void noAuthorizationHeader_returns401() throws Exception {
            mockMvc.perform(get("/users"))
                   .andExpect(status().isUnauthorized());
        }

        @Test
        void wrongPassword_returns401() throws Exception {
            perform(get("/users"), basicAuth(ADMIN_EMAIL, "Wrong123!"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void customer_returns403() throws Exception {
            perform(get("/users"), basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class GetUserById {

        @Test
        void admin_getsUser() throws Exception {
            perform(get("/users/{id}", customer.getId()), adminAuth)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(customer.getId().toString()))
                    .andExpect(jsonPath("$.email").value(CUSTOMER_EMAIL))
                    .andExpect(jsonPath("$.role").value("CUSTOMER"))
                    .andExpect(jsonPath("$.address.city").value("Brussels"));
        }

        @Test
        void responseNeverContainsPassword() throws Exception {
            perform(get("/users/{id}", admin.getId()), adminAuth)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        void unknownId_returns404() throws Exception {
            perform(get("/users/{id}", UUID.randomUUID()), adminAuth)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("This user is not found in the database."));
        }

        @Test
        void invalidUuid_returns400() throws Exception {
            perform(get("/users/{id}", "not-a-uuid"), adminAuth)
                    .andExpect(status().isBadRequest());
        }

        @Test
        void noAuthorizationHeader_returns401() throws Exception {
            mockMvc.perform(get("/users/{id}", customer.getId()))
                   .andExpect(status().isUnauthorized());
        }

        @Test
        void customer_returns403_evenForOwnId() throws Exception {
            perform(get("/users/{id}", customer.getId()), basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                    .andExpect(status().isForbidden());
        }

        @Test
        void authIsCheckedBeforeExistence() throws Exception {
            perform(get("/users/{id}", UUID.randomUUID()), basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                    .andExpect(status().isForbidden());
        }
    }
}