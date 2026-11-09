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

import java.util.List;

import static com.application.eurder2.data.UserJson.basicAuth;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for GET /eurders/my-report (Basic auth). */
@SpringBootTest
@Transactional
class EurderReportIntegrationTest {

    private static final String URL = "/eurders/my-report";
    private static final String ADMIN_EMAIL = "admin@eurder.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CUSTOMER_EMAIL = "customer@eurder.com";
    private static final String CUSTOMER_PASSWORD = "Customer1!";
    private static final String OTHER_EMAIL = "other@eurder.com";
    private static final String OTHER_PASSWORD = "Other123!";
    private static final String INVALID_CREDENTIALS = "These credentials are invalid.";

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
    private Item pen;   // price 2.5
    private Item book;  // price 15.0

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

    private ResultActions getReportAs(String authorization) throws Exception {
        return mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, authorization));
    }

    private ResultActions getCustomerReport() throws Exception {
        return getReportAs(basicAuth(CUSTOMER_EMAIL, CUSTOMER_PASSWORD));
    }

    @Nested
    class Success {
        @Test
        void oneEurder_returns200WithAllFields() throws Exception {
            Eurder eurder = saveEurder(customer, group(pen, 2), group(book, 1));

            getCustomerReport()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eurders", hasSize(1)))
                    .andExpect(jsonPath("$.eurders[0].eurderId").value(eurder.getId().toString()))
                    .andExpect(jsonPath("$.eurders[0].itemGroups", hasSize(2)))
                    .andExpect(jsonPath("$.eurders[0].itemGroups[0].itemName").value("Pen"))
                    .andExpect(jsonPath("$.eurders[0].itemGroups[0].amount").value(2))
                    .andExpect(jsonPath("$.eurders[0].itemGroups[0].groupPrice").value(5.0))
                    .andExpect(jsonPath("$.eurders[0].itemGroups[1].itemName").value("Book"))
                    .andExpect(jsonPath("$.eurders[0].itemGroups[1].amount").value(1))
                    .andExpect(jsonPath("$.eurders[0].itemGroups[1].groupPrice").value(15.0))
                    .andExpect(jsonPath("$.eurders[0].eurderPrice").value(20.0))
                    .andExpect(jsonPath("$.totalPrice").value(20.0));
        }

        @Test
        void severalEurders_totalPriceIsSumOfAllEurders() throws Exception {
            saveEurder(customer, group(pen, 2));   // 5.0
            saveEurder(customer, group(book, 2));  // 30.0

            getCustomerReport()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eurders", hasSize(2)))
                    .andExpect(jsonPath("$.eurders[*].eurderPrice", containsInAnyOrder(5.0, 30.0)))
                    .andExpect(jsonPath("$.totalPrice").value(35.0));
        }

        @Test
        void noEurders_returnsEmptyListAndZeroTotal() throws Exception {
            getCustomerReport()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eurders", hasSize(0)))
                    .andExpect(jsonPath("$.totalPrice").value(0.0));
        }

        @Test
        void onlyOwnEurders_areInReport() throws Exception {
            Eurder mine = saveEurder(customer, group(pen, 1));
            saveEurder(other, group(book, 3));
            saveEurder(admin, group(book, 1));

            getCustomerReport()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eurders", hasSize(1)))
                    .andExpect(jsonPath("$.eurders[0].eurderId").value(mine.getId().toString()))
                    .andExpect(jsonPath("$.totalPrice").value(2.5));
        }

        @Test
        void itemPriceChangedAfterOrder_reportKeepsOldPrice() throws Exception {
            saveEurder(customer, group(pen, 2));   // 2 x 2.5
            pen.setPrice(100.0);
            itemRepository.save(pen);

            getCustomerReport()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eurders[0].itemGroups[0].groupPrice").value(5.0))
                    .andExpect(jsonPath("$.eurders[0].eurderPrice").value(5.0))
                    .andExpect(jsonPath("$.totalPrice").value(5.0));
        }

        @Test
        void admin_seesOwnReport() throws Exception {
            saveEurder(admin, group(book, 1));
            saveEurder(customer, group(pen, 1));

            getReportAs(basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eurders", hasSize(1)))
                    .andExpect(jsonPath("$.totalPrice").value(15.0));
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
            getReportAs(basicAuth(CUSTOMER_EMAIL, "Wrong123!"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
        }

        @Test
        void unknownUser_returns401() throws Exception {
            getReportAs(basicAuth("nobody@eurder.com", CUSTOMER_PASSWORD))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void notBasicScheme_returns401() throws Exception {
            getReportAs("Bearer some-token")
                    .andExpect(status().isUnauthorized());
        }
    }
}