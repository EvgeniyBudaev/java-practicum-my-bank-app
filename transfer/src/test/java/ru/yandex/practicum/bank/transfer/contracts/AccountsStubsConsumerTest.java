package ru.yandex.practicum.bank.transfer.contracts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(properties = "bank.security.enabled=false")
@EmbeddedKafka(partitions = 1, topics = "bank.notifications")
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH,
        ids = "ru.yandex.practicum:my-bank-accounts:+:stubs:9561"
)
class AccountsStubsConsumerTest {

    private final RestClient client = RestClient.builder()
            .baseUrl("http://localhost:9561")
            .build();

    @Test
    void shouldGetMe() {
        String body = client.get()
                .uri("/api/accounts/me")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("name");
        assertThat(body).contains("birthdate");
        assertThat(body).contains("sum");
    }

    @Test
    void shouldReturn409OnWithdrawNotEnough() {
        try {
            client.post()
                    .uri("/internal/accounts/oleg/withdraw")
                    .header("Content-Type", "application/json")
                    .body("{\"amount\":999999}")
                    .retrieve()
                    .toBodilessEntity();
            fail("Expected 409");
        } catch (RestClientResponseException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(e.getResponseBodyAsString()).contains("Недостаточно средств");
        }
    }
}
