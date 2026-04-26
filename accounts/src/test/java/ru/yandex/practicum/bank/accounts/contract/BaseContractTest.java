package ru.yandex.practicum.bank.accounts.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;

@SpringBootTest(properties = {
        "bank.security.enabled=false"
})
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = "bank.notifications")
@WithMockUser(username = "testuser")
public abstract class BaseContractTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        jdbc.execute("DELETE FROM accounts");
        jdbc.update("INSERT INTO accounts (login, name, birthdate, balance) VALUES (?, ?, ?, ?)",
                "testuser", "testuser", Date.valueOf(LocalDate.of(1990, 1, 1)), 0L);
        jdbc.update("INSERT INTO accounts (login, name, birthdate, balance) VALUES (?, ?, ?, ?)",
                "oleg", "Олег", Date.valueOf(LocalDate.of(1985, 5, 10)), 1000L);
    }
}
