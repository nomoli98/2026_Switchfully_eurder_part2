package com.application.eurder2.controller;

import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.Address;
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

import static com.application.eurder2.data.UserJson.basicAuth;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for GET /eurders/shipping-today (Basic auth, admin only). */
@SpringBootTest
@Transactional
class ShippingTodayIntegrationTest {

    private static final String URL = "/eurders/shipping-today";
    private static final String ADMIN_EMAIL = "admin@eurder.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CUSTOMER_EMAIL = "customer@eurder.com";
    private static final String CUSTOMER_PASSWORD = "Customer1!";
    private static final String OTHER_EMAIL = "other@eurder.com";
    private static final String OTHER_PASSWORD = "Other123!";
    private static final String INVALID_CREDENTIALS = "These credentials are invalid.";
    private static final String NO_PERMISSION = "You do not have the authorizations to go further.";

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
    private User customer;
    private User other;
    private Item pen;   // price 2.5
    private Item book;  // price 15.0
    private LocalDate today;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        saveUser(ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
        customer = saveUser(CUSTOMER_EMAIL, CUSTOMER_PASSWORD, Role.CUSTOMER);
        other = saveUser(OTHER_EMAIL, OTHER_PASSWORD, Role.CUSTOMER);
        other.setAddress(new Address("Station Road", "5", "Ghent", "9000", "Belgium"));
        userRepository.save(other);
        pen = itemRepository.save(TestData.newItem("Pen", 2.5, 10));
        book = itemRepository.save(TestData.newItem("Book", 15.0, 10));
        today = LocalDate.now();
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

    /** Shipping date is normally computed; tests force it. */
    private ItemGroup group(Item item, int amount, LocalDate shippingDate) {
        ItemGroup group = new ItemGroup(item, amount);
        group.setShippingDate(shippingDate);
        return group;
    }

    private ResultActions getAs(String authorization) throws Exception {
        return mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, authorization));
    }

    private ResultActions getAsAdmin() throws Exception {
        return getAs(basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD));
    }

    @Nested
    class Success {
        @Test
        void oneEurderShippingToday_returns200WithGroupsAndAddress() throws Exception {
            saveEurder(customer, group(pen, 2, today), group(book, 1, today));

            getAsAdmin()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].itemGroup", hasSize(2)))
                    .andExpect(jsonPath("$[0].itemGroup[0].itemName").value("Pen"))
                    .andExpect(jsonPath("$[0].itemGroup[0].amount").value(2))
                    .andExpect(jsonPath("$[0].itemGroup[0].groupPrice").value(5.0))
                    .andExpect(jsonPath("$[0].itemGroup[1].itemName").value("Book"))
                    .andExpect(jsonPath("$[0].itemGroup[1].amount").value(1))
                    .andExpect(jsonPath("$[0].itemGroup[1].groupPrice").value(15.0))
                    .andExpect(jsonPath("$[0].shippingAddress.street").value("Main Street"))
                    .andExpect(jsonPath("$[0].shippingAddress.number").value("12A"))
                    .andExpect(jsonPath("$[0].shippingAddress.city").value("Brussels"))
                    .andExpect(jsonPath("$[0].shippingAddress.postalCode").value("1000"))
                    .andExpect(jsonPath("$[0].shippingAddress.country").value("Belgium"));
        }

        @Test
        void mixedEurder_returnsOnlyGroupsShippingToday() throws Exception {
            saveEurder(customer, group(pen, 2, today), group(book, 1, today.plusDays(7)));

            getAsAdmin()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].itemGroup", hasSize(1)))
                    .andExpect(jsonPath("$[0].itemGroup[0].itemName").value("Pen"));
        }

        @Test
        void eurdersNotShippingToday_areExcluded() throws Exception {
            saveEurder(customer, group(pen, 1, today.plusDays(1)));
            saveEurder(customer, group(pen, 1, today.minusDays(1)));
            saveEurder(customer, group(book, 1, today.plusDays(7)));

            getAsAdmin()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void severalCustomers_eachEurderHasOwnAddress() throws Exception {
            saveEurder(customer, group(pen, 1, today));
            saveEurder(other, group(book, 1, today));

            getAsAdmin()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].shippingAddress.city", containsInAnyOrder("Brussels", "Ghent")));
        }

        @Test
        void groupPrice_usesPriceAtOrder_notCurrentPrice() throws Exception {
            saveEurder(customer, group(pen, 2, today)); // 2 x 2.5
            pen.setPrice(100.0);
            itemRepository.save(pen);

            getAsAdmin()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].itemGroup[0].groupPrice").value(5.0));
        }

        @Test
        void nothingShipsToday_returnsEmptyList() throws Exception {
            getAsAdmin()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    class Forbidden {
        @Test
        void customer_returns403() throws Exception {
            saveEurder(customer, group(pen, 1, today));

            getAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(NO_PERMISSION));
        }
    }

    @Nested
    class Unauthorized {
        @Test
        void noAuthorizationHeader_returns401() throws Exception {
            mockMvc.perform(get(URL))
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void wrongPassword_returns401() throws Exception {
            getAs(basicAuth(ADMIN_EMAIL, "Wrong123!"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void unknownUser_returns401() throws Exception {
            getAs(basicAuth("nobody@eurder.com", ADMIN_PASSWORD))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void notBasicScheme_returns401() throws Exception {
            getAs("Bearer some-token")
                    .andExpect(status().isUnauthorized());
        }
    }
}