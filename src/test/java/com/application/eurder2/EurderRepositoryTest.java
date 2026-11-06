package com.application.eurder2;

import com.application.eurder2.repository.EurderRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class EurderRepositoryTest {

    @Autowired
    private EurderRepository eurderRepository;

    @Test
    void savesEurderWithItemGroups() {
        // create user, item, eurder, addItemGroup(...), save, assert
    }
}
