package com.application.eurder2.controller;

import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.enums.Role;
import com.application.eurder2.repository.EurderRepository;
import com.application.eurder2.repository.ItemRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.application.eurder2.data.UserJson.basicAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for POST /eurders/re-eurder/{id} (Basic auth). */
@SpringBootTest
@Transactional
class ReEurderIntegrationTest {

    private static final String URL = "/eurders/re-eurder/{id}";
    private static final String ADMIN_EMAIL = "admin@eurder.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CUSTOMER_EMAIL = "customer@eurder.com";
    private static final String CUSTOMER_PASSWORD = "Customer1!";
    private static final String OTHER_EMAIL = "other@eurder.com";
    private static final String OTHER_PASSWORD = "Other123!";
    private static final String INVALID_CREDENTIALS = "These credentials are invalid.";
    private static final String EURDER_NOT_FOUND = "This eurder is not found in the database.";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private EurderRepository eurderRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private User admin;
    private User customer;
    private User other;
    private Item pen;   // price 2.5, stock 10
    private Item book;  // price 15.0, stock 10

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        admin = saveUser(ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
        customer = saveUser(CUSTOMER_EMAIL, CUSTOMER_PASSWORD, Role.CUSTOMER);
        other = saveUser(OTHER_EMAIL, OTHER_PASSWORD, Role.CUSTOMER);
        pen = itemRepository.save(TestData.newItem("Pen", 2.5, 10));
        book = itemRepository.save(TestData.newItem("Book", 15.0, 10));
    }

    private User saveUser(String email, String rawPassword, Role role) {
        User user = TestData.newUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    private Eurder saveEurder(User user, ItemGroup... groups) {
        return eurderRepository.save(new Eurder(List.of(groups), user));
    }

    private ItemGroup group(Item item, int amount) {
        return new ItemGroup(item, amount);
    }

    private ResultActions reEurderAs(String authorization, Object eurderId) throws Exception {
        return mockMvc.perform(post(URL, eurderId).header(HttpHeaders.AUTHORIZATION, authorization));
    }

    private ResultActions reEurderAsCustomer(Object eurderId) throws Exception {
        return reEurderAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD), eurderId);
    }

    @Nested
    class Success {
        @Test
        void ownEurder_returns201WithNewEurder() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 2), group(book, 1));

            reEurderAsCustomer(original.getId())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.eurderId").isNotEmpty())
                    .andExpect(jsonPath("$.eurderId", not(original.getId().toString())))
                    .andExpect(jsonPath("$.userId").value(customer.getId().toString()))
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.items[0].itemName").value("Pen"))
                    .andExpect(jsonPath("$.items[0].amount").value(2))
                    .andExpect(jsonPath("$.items[0].groupPrice").value(5.0))
                    .andExpect(jsonPath("$.items[1].itemName").value("Book"))
                    .andExpect(jsonPath("$.items[1].amount").value(1))
                    .andExpect(jsonPath("$.items[1].groupPrice").value(15.0))
                    .andExpect(jsonPath("$.eurderPrice").value(20.0));
        }

        @Test
        void newEurderIsSaved_originalIsUnchanged() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 2));
            long before = eurderRepository.count();

            reEurderAsCustomer(original.getId()).andExpect(status().isCreated());

            assertThat(eurderRepository.count()).isEqualTo(before + 1);
            assertThat(eurderRepository.findById(original.getId())).get()
                                                                   .satisfies(e -> {
                                                                       assertThat(e.getItemGroups()).hasSize(1);
                                                                       assertThat(e.getTotalPrice()).isEqualTo(5.0);
                                                                   });
        }

        @Test
        void usesCurrentItemPrice_notOldPrice() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 2));   // 2 x 2.5
            pen.setPrice(100.0);
            itemRepository.save(pen);

            reEurderAsCustomer(original.getId())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items[0].groupPrice").value(200.0))
                    .andExpect(jsonPath("$.eurderPrice").value(200.0));
        }

        @Test
        void stockIsDecreased() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 3));

            reEurderAsCustomer(original.getId()).andExpect(status().isCreated());

            assertThat(itemRepository.findById(pen.getId())).get()
                                                            .extracting(Item::getStock).isEqualTo(7);
        }

        @Test
        void enoughStock_shipsTomorrow() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 1));

            reEurderAsCustomer(original.getId())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items[0].shippingDate").value(LocalDate.now().plusDays(1).toString()));
        }

        @Test
        void notEnoughStockAnymore_shipsInSevenDays() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 5));
            pen.setStock(2);
            itemRepository.save(pen);

            reEurderAsCustomer(original.getId())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items[0].shippingDate").value(LocalDate.now().plusDays(7).toString()));
        }

        @Test
        void sameEurderCanBeReorderedTwice() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 1));
            long before = eurderRepository.count();

            reEurderAsCustomer(original.getId()).andExpect(status().isCreated());
            reEurderAsCustomer(original.getId()).andExpect(status().isCreated());

            assertThat(eurderRepository.count()).isEqualTo(before + 2);
        }

        @Test
        void admin_canReorderOwnEurder() throws Exception {
            Eurder original = saveEurder(admin, group(book, 1));

            reEurderAs(basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD), original.getId())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(admin.getId().toString()));
        }
    }

    @Nested
    class NotFound {
        @Test
        void unknownEurderId_returns404() throws Exception {
            reEurderAsCustomer(UUID.randomUUID())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(EURDER_NOT_FOUND));
        }

        @Test
        void eurderOfOtherCustomer_returns404_andSavesNothing() throws Exception {
            Eurder notMine = saveEurder(other, group(pen, 2));
            long before = eurderRepository.count();

            reEurderAsCustomer(notMine.getId())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(EURDER_NOT_FOUND));

            assertThat(eurderRepository.count()).isEqualTo(before);
            assertThat(itemRepository.findById(pen.getId())).get()
                                                            .extracting(Item::getStock).isEqualTo(10);
        }

        @Test
        void admin_cannotReorderCustomerEurder_returns404() throws Exception {
            Eurder customerEurder = saveEurder(customer, group(pen, 1));

            reEurderAs(basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD), customerEurder.getId())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class BadRequest {
        @Test
        void invalidUuid_returns400() throws Exception {
            reEurderAsCustomer("not-a-uuid")
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Unauthorized {
        @Test
        void noAuthorizationHeader_returns401() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 1));

            mockMvc.perform(post(URL, original.getId()))
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void wrongPassword_returns401() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 1));

            reEurderAs(basicAuth(CUSTOMER_EMAIL, "Wrong123!"), original.getId())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void unknownUser_returns401() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 1));

            reEurderAs(basicAuth("nobody@eurder.com", CUSTOMER_PASSWORD), original.getId())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void notBasicScheme_returns401() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 1));

            reEurderAs("Bearer some-token", original.getId())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void failedAuth_savesNothing_andKeepsStock() throws Exception {
            Eurder original = saveEurder(customer, group(pen, 1));
            long before = eurderRepository.count();

            reEurderAs(basicAuth(CUSTOMER_EMAIL, "Wrong123!"), original.getId());

            assertThat(eurderRepository.count()).isEqualTo(before);
            assertThat(itemRepository.findById(pen.getId())).get()
                                                            .extracting(Item::getStock).isEqualTo(10);
        }
    }
}