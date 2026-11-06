package com.application.eurder2.controller;

import com.application.eurder2.data.EurderJson;
import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.User;
import com.application.eurder2.enums.Role;
import com.application.eurder2.repository.EurderRepository;
import com.application.eurder2.repository.ItemGroupRepository;
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

import java.time.LocalDate;
import java.util.UUID;

import static com.application.eurder2.data.UserJson.basicAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for POST /eurders (customer and admin, Basic auth). */
@SpringBootTest
@Transactional
class CreateEurderIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@eurder.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CUSTOMER_EMAIL = "customer@eurder.com";
    private static final String CUSTOMER_PASSWORD = "Customer1!";
    private static final String INVALID_CREDENTIALS = "These credentials are invalid.";
    private static final String ITEM_NOT_FOUND = "This item is not found in the database.";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private EurderRepository eurderRepository;
    @Autowired
    private ItemGroupRepository itemGroupRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private String customerAuth;
    private User customer;
    private Item pen;   // price 2.5, stock 10
    private Item book;  // price 15.0, stock 1

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        saveUser(ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
        customer = saveUser(CUSTOMER_EMAIL, CUSTOMER_PASSWORD, Role.CUSTOMER);
        customerAuth = basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        pen = itemRepository.save(TestData.newItem("Pen", 2.5, 10));
        book = itemRepository.save(TestData.newItem("Book", 15.0, 1));
    }

    private User saveUser(String email, String rawPassword, Role role) {
        User user = TestData.newUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    private MockHttpServletRequestBuilder createRequest(String body) {
        return post("/eurders").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private ResultActions createAs(String authorization, String body) throws Exception {
        return mockMvc.perform(createRequest(body).header(HttpHeaders.AUTHORIZATION, authorization));
    }

    private String penOrder() {
        return EurderJson.order().item(pen.getId(), 2).build();
    }

    @Nested
    class Success {
        @Test
        void customer_createsEurder_returns201WithBody() throws Exception {
            createAs(customerAuth, penOrder())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.eurderId", notNullValue()))
                    .andExpect(jsonPath("$.userId").value(customer.getId().toString()))
                    .andExpect(jsonPath("$.eurderPrice").value(5.0))
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].itemName").value("Pen"))
                    .andExpect(jsonPath("$.items[0].amount").value(2))
                    .andExpect(jsonPath("$.items[0].groupPrice").value(5.0));
        }

        @Test
        void admin_canAlsoCreateEurder() throws Exception {
            createAs(basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD), penOrder())
                    .andExpect(status().isCreated());
        }

        @Test
        void severalItems_totalPriceIsSumOfGroups() throws Exception {
            createAs(customerAuth, EurderJson.order().item(pen.getId(), 4).item(book.getId(), 1).build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.items[0].itemName").value("Pen"))
                    .andExpect(jsonPath("$.items[1].itemName").value("Book"))
                    .andExpect(jsonPath("$.eurderPrice").value(25.0));
        }

        @Test
        void enoughStock_shipsTomorrow() throws Exception {
            createAs(customerAuth, penOrder())
                    .andExpect(jsonPath("$.items[0].shippingDate")
                                       .value(LocalDate.now().plusDays(1).toString()));
        }

        @Test
        void notEnoughStock_shipsInSevenDays() throws Exception {
            createAs(customerAuth, EurderJson.order().item(book.getId(), 3).build())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items[0].shippingDate")
                                       .value(LocalDate.now().plusDays(7).toString()));
        }

        @Test
        void stockIsDecreased() throws Exception {
            createAs(customerAuth, penOrder()).andExpect(status().isCreated());

            assertThat(itemRepository.findById(pen.getId()).orElseThrow().getStock()).isEqualTo(8);
        }

        @Test
        void stockNeverGoesBelowZero() throws Exception {
            createAs(customerAuth, EurderJson.order().item(book.getId(), 3).build())
                    .andExpect(status().isCreated());

            assertThat(itemRepository.findById(book.getId()).orElseThrow().getStock()).isZero();
        }

        @Test
        void eurderAndGroups_areSavedInDatabase() throws Exception {
            long eurdersBefore = eurderRepository.count();
            long groupsBefore = itemGroupRepository.count();

            createAs(customerAuth, EurderJson.order().item(pen.getId(), 1).item(book.getId(), 1).build())
                    .andExpect(status().isCreated());

            assertThat(eurderRepository.count()).isEqualTo(eurdersBefore + 1);
            assertThat(itemGroupRepository.count()).isEqualTo(groupsBefore + 2);
        }
    }

    @Nested
    class Unauthorized {
        @Test
        void noAuthorizationHeader_returns401() throws Exception {
            mockMvc.perform(createRequest(penOrder()))
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void wrongPassword_returns401() throws Exception {
            createAs(basicAuth(CUSTOMER_EMAIL, "Wrong123!"), penOrder())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void unknownUser_returns401() throws Exception {
            createAs(basicAuth("nobody@eurder.com", CUSTOMER_PASSWORD), penOrder())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void notBasicScheme_returns401() throws Exception {
            createAs("Bearer some-token", penOrder())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void failedAuth_savesNothing_andKeepsStock() throws Exception {
            long before = eurderRepository.count();

            mockMvc.perform(createRequest(penOrder()));

            assertThat(eurderRepository.count()).isEqualTo(before);
            assertThat(itemRepository.findById(pen.getId()).orElseThrow().getStock()).isEqualTo(10);
        }
    }

    @Nested
    class Validation {
        @Test
        void emptyList_returns400() throws Exception {
            createAs(customerAuth, EurderJson.emptyList())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.itemGroupRequests", notNullValue()));
        }

        @Test
        void missingList_returns400() throws Exception {
            createAs(customerAuth, EurderJson.noList())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.itemGroupRequests", notNullValue()));
        }

        @Test
        void nullElement_returns400() throws Exception {
            createAs(customerAuth, EurderJson.order().nullItem().build())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void missingItemId_returns400() throws Exception {
            createAs(customerAuth, EurderJson.order().itemWithoutId(1).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$['itemGroupRequests[0].itemId']", notNullValue()));
        }

        @Test
        void missingAmount_returns400() throws Exception {
            createAs(customerAuth, EurderJson.order().itemWithoutAmount(pen.getId()).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$['itemGroupRequests[0].amount']", notNullValue()));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void amountNotPositive_returns400(int amount) throws Exception {
            createAs(customerAuth, EurderJson.order().item(pen.getId(), amount).build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$['itemGroupRequests[0].amount']", notNullValue()));
        }

        @Test
        void invalidUuid_returns400() throws Exception {
            createAs(customerAuth, EurderJson.order().itemWithRawId("not-a-uuid", 1).build())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void invalidBody_savesNothing_andKeepsStock() throws Exception {
            long before = eurderRepository.count();

            createAs(customerAuth, EurderJson.order().item(pen.getId(), 0).build());

            assertThat(eurderRepository.count()).isEqualTo(before);
            assertThat(itemRepository.findById(pen.getId()).orElseThrow().getStock()).isEqualTo(10);
        }
    }

    @Nested
    class UnknownItem {
        @Test
        void unknownItemId_returns404() throws Exception {
            createAs(customerAuth, EurderJson.order().item(UUID.randomUUID(), 1).build())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ITEM_NOT_FOUND));
        }

        @Test
        void oneUnknownAmongValid_returns404_andSavesNothing() throws Exception {
            long before = eurderRepository.count();

            createAs(customerAuth, EurderJson.order().item(pen.getId(), 1).item(UUID.randomUUID(), 1).build())
                    .andExpect(status().isNotFound());

            assertThat(eurderRepository.count()).isEqualTo(before);
        }
    }
}