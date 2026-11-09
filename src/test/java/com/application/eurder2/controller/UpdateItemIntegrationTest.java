package com.application.eurder2.controller;

import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.User;
import com.application.eurder2.enums.Role;
import com.application.eurder2.repository.ItemRepository;
import com.application.eurder2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import java.util.UUID;

import static com.application.eurder2.data.UserJson.basicAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for PATCH /items/{id} (admin only, Basic auth). */
@SpringBootTest
@Transactional
class UpdateItemIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@eurder.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CUSTOMER_EMAIL = "customer@eurder.com";
    private static final String CUSTOMER_PASSWORD = "Customer1!";
    private static final String INVALID_CREDENTIALS = "These credentials are invalid.";
    private static final String FORBIDDEN = "You do not have the authorizations to go further.";
    private static final String NOT_FOUND = "This item is not found in the database.";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private String adminAuth;
    private UUID penId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        saveUser(ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
        saveUser(CUSTOMER_EMAIL, CUSTOMER_PASSWORD, Role.CUSTOMER);
        adminAuth = basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD);
        penId = itemRepository.save(TestData.newItem("Pen", 2.5, 10)).getId();
    }

    private void saveUser(String email, String rawPassword, Role role) {
        User user = TestData.newUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }

    private MockHttpServletRequestBuilder updateRequest(UUID id, String body) {
        return patch("/items/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private ResultActions updateAs(String authorization, UUID id, String body) throws Exception {
        return mockMvc.perform(updateRequest(id, body).header(HttpHeaders.AUTHORIZATION, authorization));
    }

    private Item pen() {
        return itemRepository.findById(penId).orElseThrow();
    }

    @Nested
    class Success {
        @Test
        void admin_updatesAllFields_returns200WithBody() throws Exception {
            String body = """
                    {"name":"Pencil","description":"Black pencil","price":1.0,"stock":5}""";

            updateAs(adminAuth, penId, body)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(penId.toString()))
                    .andExpect(jsonPath("$.name").value("Pencil"))
                    .andExpect(jsonPath("$.description").value("Black pencil"))
                    .andExpect(jsonPath("$.price").value(1.0))
                    .andExpect(jsonPath("$.stock").value(5));
        }

        @Test
        void updatedItem_isSavedInDatabase() throws Exception {
            String body = """
                    {"name":"Pencil","description":"Black pencil","price":1.0,"stock":5}""";

            updateAs(adminAuth, penId, body).andExpect(status().isOk());

            Item saved = pen();
            assertThat(saved.getName()).isEqualTo("Pencil");
            assertThat(saved.getDescription()).isEqualTo("Black pencil");
            assertThat(saved.getPrice()).isEqualTo(1.0);
            assertThat(saved.getStock()).isEqualTo(5);
        }

        @Test
        void onlyStock_changesOnlyStock() throws Exception {
            updateAs(adminAuth, penId, "{\"stock\":42}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Pen"))
                    .andExpect(jsonPath("$.description").value("Pen description"))
                    .andExpect(jsonPath("$.price").value(2.5))
                    .andExpect(jsonPath("$.stock").value(42));
        }

        @Test
        void emptyBody_changesNothing() throws Exception {
            updateAs(adminAuth, penId, "{}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Pen"))
                    .andExpect(jsonPath("$.stock").value(10));
        }

        @Test
        void blankText_isIgnored() throws Exception {
            updateAs(adminAuth, penId, "{\"name\":\"  \",\"description\":\"\"}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Pen"))
                    .andExpect(jsonPath("$.description").value("Pen description"));
        }

        @Test
        void priceAndStockZero_areAccepted() throws Exception {
            updateAs(adminAuth, penId, "{\"price\":0,\"stock\":0}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(0.0))
                    .andExpect(jsonPath("$.stock").value(0));
        }

        @Test
        void update_doesNotCreateNewItem() throws Exception {
            long before = itemRepository.count();
            updateAs(adminAuth, penId, "{\"name\":\"Pencil\"}").andExpect(status().isOk());
            assertThat(itemRepository.count()).isEqualTo(before);
        }
    }

    @Nested
    class NotFound {
        @Test
        void unknownId_returns404() throws Exception {
            updateAs(adminAuth, UUID.randomUUID(), "{\"name\":\"Pencil\"}")
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(NOT_FOUND));
        }
    }

    @Nested
    class Unauthorized {
        @Test
        void noAuthorizationHeader_returns401() throws Exception {
            mockMvc.perform(updateRequest(penId, "{\"name\":\"Pencil\"}"))
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void wrongPassword_returns401() throws Exception {
            updateAs(basicAuth(ADMIN_EMAIL, "Wrong123!"), penId, "{\"name\":\"Pencil\"}")
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void failedAuth_changesNothing() throws Exception {
            mockMvc.perform(updateRequest(penId, "{\"name\":\"Pencil\"}"));
            assertThat(pen().getName()).isEqualTo("Pen");
        }
    }

    @Nested
    class Forbidden {
        @Test
        void customer_returns403() throws Exception {
            updateAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD), penId, "{\"name\":\"Pencil\"}")
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(FORBIDDEN));
        }

        @Test
        void customer_changesNothing() throws Exception {
            updateAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD), penId, "{\"name\":\"Pencil\"}");
            assertThat(pen().getName()).isEqualTo("Pen");
        }
    }

    @Nested
    class Validation {
        @Test
        void negativePrice_returns400() throws Exception {
            updateAs(adminAuth, penId, "{\"price\":-0.01}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.price", notNullValue()));
        }

        @Test
        void negativeStock_returns400() throws Exception {
            updateAs(adminAuth, penId, "{\"stock\":-1}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.stock", notNullValue()));
        }

        @Test
        void descriptionOver255Chars_returns400() throws Exception {
            updateAs(adminAuth, penId, "{\"description\":\"" + "a".repeat(256) + "\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.description", notNullValue()));
        }

        @Test
        void invalidBody_changesNothing() throws Exception {
            updateAs(adminAuth, penId, "{\"name\":\"Pencil\",\"stock\":-1}");
            assertThat(pen().getName()).isEqualTo("Pen");
            assertThat(pen().getStock()).isEqualTo(10);
        }
    }
}