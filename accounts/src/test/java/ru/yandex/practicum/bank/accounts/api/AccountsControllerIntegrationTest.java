package ru.yandex.practicum.bank.accounts.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "bank.security.enabled=false")
@AutoConfigureMockMvc
@WithMockUser(username = "testuser")
class AccountsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("DELETE FROM accounts");
        jdbc.update("INSERT INTO accounts (login, name, birthdate, balance) VALUES (?, ?, ?, ?)",
                "testuser", "Test User", Date.valueOf(LocalDate.of(1990, 1, 1)), 5000L);
        jdbc.update("INSERT INTO accounts (login, name, birthdate, balance) VALUES (?, ?, ?, ?)",
                "other", "Other User", Date.valueOf(LocalDate.of(1985, 6, 15)), 3000L);
    }

    @Test
    void getMe_returnsAccountData() throws Exception {
        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.sum", is(5000)));
    }

    @Test
    void editMe_updatesProfile() throws Exception {
        mockMvc.perform(patch("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated Name", "birthdate": "1990-06-15"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    void editMe_underAge_returns400() throws Exception {
        mockMvc.perform(patch("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Young User", "birthdate": "2015-01-01"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecipients_excludesSelf() throws Exception {
        mockMvc.perform(get("/api/accounts/recipients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].login", is("other")));
    }

    @Test
    void internalDeposit_increasesBalance() throws Exception {
        mockMvc.perform(post("/internal/accounts/testuser/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 1000}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(jsonPath("$.sum", is(6000)));
    }

    @Test
    void internalWithdraw_sufficientFunds_decreasesBalance() throws Exception {
        mockMvc.perform(post("/internal/accounts/testuser/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 2000}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(jsonPath("$.sum", is(3000)));
    }

    @Test
    void internalWithdraw_insufficientFunds_returns409() throws Exception {
        mockMvc.perform(post("/internal/accounts/testuser/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 99999}
                                """))
                .andExpect(status().isConflict());
    }
}
