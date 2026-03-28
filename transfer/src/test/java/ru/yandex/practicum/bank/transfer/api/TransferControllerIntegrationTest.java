package ru.yandex.practicum.bank.transfer.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.transfer.client.AccountsClient;
import ru.yandex.practicum.bank.transfer.client.NotificationsClient;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "bank.security.enabled=false")
@AutoConfigureMockMvc
class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountsClient accountsClient;

    @MockitoBean
    private NotificationsClient notificationsClient;

    @Test
    void transfer_validRequest_returns200() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sender")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": 500, "login": "receiver"}
                                """))
                .andExpect(status().isOk());

        verify(accountsClient).withdraw("sender", 500);
        verify(accountsClient).deposit("receiver", 500);
    }

    @Test
    void transfer_blankLogin_returns400() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sender")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": 500, "login": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_invalidValue_returns400() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sender")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": -100, "login": "receiver"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
