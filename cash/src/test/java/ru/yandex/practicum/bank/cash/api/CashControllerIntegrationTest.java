package ru.yandex.practicum.bank.cash.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.cash.client.AccountsClient;
import ru.yandex.practicum.bank.cash.client.NotificationsClient;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "bank.security.enabled=false")
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = "bank.notifications")
class CashControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountsClient accountsClient;

    @MockitoBean
    private NotificationsClient notificationsClient;

    @Test
    void cash_deposit_returns200() throws Exception {
        mockMvc.perform(post("/api/cash")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": 500, "action": "PUT"}
                                """))
                .andExpect(status().isOk());

        verify(accountsClient).deposit("testuser", 500);
    }

    @Test
    void cash_withdraw_returns200() throws Exception {
        mockMvc.perform(post("/api/cash")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": 300, "action": "GET"}
                                """))
                .andExpect(status().isOk());

        verify(accountsClient).withdraw("testuser", 300);
    }

    @Test
    void cash_invalidValue_returns400() throws Exception {
        mockMvc.perform(post("/api/cash")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": -10, "action": "PUT"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
