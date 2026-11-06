package com.application.eurder2.controller;

import com.application.eurder2.data.ItemJson;
import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.User;
import com.application.eurder2.enums.Role;
import com.application.eurder2.repository.ItemRepository;
import com.application.eurder2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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

import java.util.UUID;

import static com.application.eurder2.data.UserJson.basicAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for POST /items (admin only, Basic auth). */
@SpringBootTest
@Transactional
class CreateItemIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@eurder.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CUSTOMER_EMAIL = "customer@eurder.com";
    private static final String CUSTOMER_PASSWORD = "Customer1!";
    private static final String INVALID_CREDENTIALS = "These credentials are invalid.";
    private static final String FORBIDDEN = "You do not have the authorizations to go further.";

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
        userRepository.save(user);
    }

    private MockHttpServletRequestBuilder createRequest(String body) {
        return post("/items").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private ResultActions createAs(String authorization, String body) throws Exception {
        return mockMvc.perform(createRequest(body).header(HttpHeaders.AUTHORIZATION, authorization));
    }

    @Nested
    class Success {
        @Test
        void admin_createsItem_returns201WithBody() throws Exception {
            createAs(adminAuth, ItemJson.valid().build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name").value("Laptop"))
                    .andExpect(jsonPath("$.description").value("A fast laptop"))
                    .andExpect(jsonPath("$.price").value(999.99))
                    .andExpect(jsonPath("$.stock").value(10));
        }

        @Test
        void createdItem_isSavedInDatabase() throws Exception {
            String body = createAs(adminAuth, ItemJson.valid().build())
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            UUID id = UUID.fromString(body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1"));
            Item saved = itemRepository.findById(id).orElseThrow();
            assertThat(saved.getName()).isEqualTo("Laptop");
            assertThat(saved.getDescription()).isEqualTo("A fast laptop");
            assertThat(saved.getPrice()).isEqualTo(999.99);
            assertThat(saved.getStock()).isEqualTo(10);
        }

        @Test
        void priceAndStockZero_areAccepted() throws Exception {
            createAs(adminAuth, ItemJson.valid().with("price", 0).with("stock", 0).build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.price").value(0.0))
                    .andExpect(jsonPath("$.stock").value(0));
        }

        @Test
        void sameNameTwice_createsTwoItems() throws Exception {
            long before = itemRepository.count();

            createAs(adminAuth, ItemJson.valid().build()).andExpect(status().isCreated());
            createAs(adminAuth, ItemJson.valid().build()).andExpect(status().isCreated());

            assertThat(itemRepository.count()).isEqualTo(before + 2);
        }

        @Test
        void descriptionOf255Chars_isAccepted() throws Exception {
            createAs(adminAuth, ItemJson.valid().with("description", "a".repeat(255)).build())
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    class Unauthorized {
        @Test
        void noAuthorizationHeader_returns401() throws Exception {
            mockMvc.perform(createRequest(ItemJson.valid().build()))
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void wrongPassword_returns401() throws Exception {
            createAs(basicAuth(ADMIN_EMAIL, "Wrong123!"), ItemJson.valid().build())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void unknownUser_returns401() throws Exception {
            createAs(basicAuth("nobody@eurder.com", ADMIN_PASSWORD), ItemJson.valid().build())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void notBasicScheme_returns401() throws Exception {
            createAs("Bearer some-token", ItemJson.valid().build())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void failedAuth_savesNothing() throws Exception {
            long before = itemRepository.count();
            mockMvc.perform(createRequest(ItemJson.valid().build()));
            assertThat(itemRepository.count()).isEqualTo(before);
        }
    }

    @Nested
    class Forbidden {
        @Test
        void customer_returns403() throws Exception {
            createAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD), ItemJson.valid().build())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(FORBIDDEN));
        }

        @Test
        void customer_savesNothing() throws Exception {
            long before = itemRepository.count();
            createAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD), ItemJson.valid().build());
            assertThat(itemRepository.count()).isEqualTo(before);
        }
    }

    @Nested
    class Validation {
        @ParameterizedTest
        @ValueSource(strings = {"name", "description", "price", "stock"})
        void missingField_returns400(String field) throws Exception {
            createAs(adminAuth, ItemJson.valid().without(field).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + field, notNullValue()));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        void blankName_returns400(String name) throws Exception {
            createAs(adminAuth, ItemJson.valid().with("name", name).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.name", notNullValue()));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        void blankDescription_returns400(String description) throws Exception {
            createAs(adminAuth, ItemJson.valid().with("description", description).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.description", notNullValue()));
        }

        @Test
        void descriptionOver255Chars_returns400() throws Exception {
            createAs(adminAuth, ItemJson.valid().with("description", "a".repeat(256)).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.description", notNullValue()));
        }

        @Test
        void negativePrice_returns400() throws Exception {
            createAs(adminAuth, ItemJson.valid().with("price", -0.01).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.price", notNullValue()));
        }

        @Test
        void negativeStock_returns400() throws Exception {
            createAs(adminAuth, ItemJson.valid().with("stock", -1).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.stock", notNullValue()));
        }

        @Test
        void invalidBody_savesNothing() throws Exception {
            long before = itemRepository.count();
            createAs(adminAuth, ItemJson.valid().with("stock", -1).build());
            assertThat(itemRepository.count()).isEqualTo(before);
        }
    }
}