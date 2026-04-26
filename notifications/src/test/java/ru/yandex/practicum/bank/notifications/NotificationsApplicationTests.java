package ru.yandex.practicum.bank.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "bank.notifications")
class NotificationsApplicationTests {

    @Test
    void contextLoads() {
    }
}
