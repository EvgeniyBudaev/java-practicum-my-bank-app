package ru.yandex.practicum.bank.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "bank.security.enabled=false")
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
