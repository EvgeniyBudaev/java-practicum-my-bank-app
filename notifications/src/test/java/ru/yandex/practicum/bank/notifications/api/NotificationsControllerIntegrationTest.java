package ru.yandex.practicum.bank.notifications.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "bank.security.enabled=false")
@AutoConfigureMockMvc
@WithMockUser(username = "service")
class NotificationsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void notify_validRequest_returns200() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "CASH_IN",
                                  "amount": 500,
                                  "actorLogin": "user1",
                                  "targetLogin": null,
                                  "occurredAt": "2026-01-01T12:00:00Z"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void notify_missingType_returns400() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 500,
                                  "actorLogin": "user1",
                                  "occurredAt": "2026-01-01T12:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notify_invalidAmount_returns400() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "CASH_OUT",
                                  "amount": -10,
                                  "actorLogin": "user1",
                                  "occurredAt": "2026-01-01T12:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
