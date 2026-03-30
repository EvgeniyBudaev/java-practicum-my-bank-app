package ru.yandex.practicum.bank.accounts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import ru.yandex.practicum.bank.accounts.client.NotificationsClient;
import ru.yandex.practicum.bank.accounts.model.Account;
import ru.yandex.practicum.bank.accounts.repo.AccountRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repo;

    @Mock
    private NotificationsClient notificationsClient;

    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Mock
    private CircuitBreaker circuitBreaker;

    private AccountService service;

    @BeforeEach
    void setUp() {
        lenient().when(circuitBreakerFactory.create(anyString())).thenReturn(circuitBreaker);
        lenient().when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        service = new AccountService(repo, notificationsClient, circuitBreakerFactory);
    }

    @Test
    void getOrCreate_existingAccount() {
        Account existing = new Account("user1", "User One", LocalDate.of(1990, 1, 1), 1000L);
        when(repo.findById("user1")).thenReturn(Optional.of(existing));

        Account result = service.getOrCreate("user1");

        assertEquals("user1", result.getLogin());
        assertEquals(1000L, result.getBalance());
        verify(repo, never()).save(any());
    }

    @Test
    void getOrCreate_newAccount() {
        when(repo.findById("newuser")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = service.getOrCreate("newuser");

        assertEquals("newuser", result.getLogin());
        assertEquals(0L, result.getBalance());
        verify(repo).save(any());
    }

    @Test
    void updateProfile_success() {
        Account existing = new Account("user1", "Old Name", LocalDate.of(1990, 1, 1), 1000L);
        when(repo.findById("user1")).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = service.updateProfile("user1", "New Name", LocalDate.of(1990, 6, 15));

        assertEquals("New Name", result.getName());
        assertEquals(LocalDate.of(1990, 6, 15), result.getBirthdate());
        verify(notificationsClient).send(any());
    }

    @Test
    void updateProfile_underAge_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile("user1", "Name", LocalDate.now().minusYears(17)));
    }

    @Test
    void updateProfile_nullBirthdate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile("user1", "Name", null));
    }

    @Test
    void deposit_success() {
        Account existing = new Account("user1", "User", LocalDate.of(1990, 1, 1), 1000L);
        when(repo.findById("user1")).thenReturn(Optional.of(existing));
        when(repo.deposit("user1", 500L)).thenReturn(1);

        assertDoesNotThrow(() -> service.deposit("user1", 500L));
        verify(repo).deposit("user1", 500L);
    }

    @Test
    void deposit_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deposit("user1", 0));
    }

    @Test
    void deposit_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deposit("user1", -100));
    }

    @Test
    void withdraw_success() {
        Account existing = new Account("user1", "User", LocalDate.of(1990, 1, 1), 1000L);
        when(repo.findById("user1")).thenReturn(Optional.of(existing));
        when(repo.withdrawIfEnough("user1", 500L)).thenReturn(1);

        assertDoesNotThrow(() -> service.withdraw("user1", 500L));
        verify(repo).withdrawIfEnough("user1", 500L);
    }

    @Test
    void withdraw_notEnoughFunds_throwsException() {
        Account existing = new Account("user1", "User", LocalDate.of(1990, 1, 1), 100L);
        when(repo.findById("user1")).thenReturn(Optional.of(existing));
        when(repo.withdrawIfEnough("user1", 500L)).thenReturn(0);

        assertThrows(NotEnoughFundsException.class,
                () -> service.withdraw("user1", 500L));
    }

    @Test
    void withdraw_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.withdraw("user1", 0));
    }

    @Test
    void recipients_returnsOtherAccounts() {
        List<Account> others = List.of(
                new Account("user2", "User Two", LocalDate.of(1985, 5, 10), 5000L));
        when(repo.findAllRecipients("user1")).thenReturn(others);

        List<Account> result = service.recipients("user1");

        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getLogin());
    }
}
