package ru.yandex.practicum.mybankfront;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "bank.security.enabled=false")
class MyBankFrontAppApplicationTests {

    @Test
    void contextLoads() {
    }

}
