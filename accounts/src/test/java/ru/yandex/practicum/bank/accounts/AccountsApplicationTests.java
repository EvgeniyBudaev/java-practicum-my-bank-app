package ru.yandex.practicum.bank.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "bank.notifications")
class AccountsApplicationTests {

    @Test
    void contextLoads() {
    }
}
