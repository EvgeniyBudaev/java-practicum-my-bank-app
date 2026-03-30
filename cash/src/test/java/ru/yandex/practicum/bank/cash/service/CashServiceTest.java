package ru.yandex.practicum.bank.cash.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import ru.yandex.practicum.bank.cash.api.dto.CashAction;
import ru.yandex.practicum.bank.cash.client.AccountsClient;
import ru.yandex.practicum.bank.cash.client.NotificationsClient;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private NotificationsClient notificationsClient;

    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Mock
    private CircuitBreaker circuitBreaker;

    private CashService service;

    @BeforeEach
    void setUp() {
        lenient().when(circuitBreakerFactory.create(anyString())).thenReturn(circuitBreaker);
        lenient().when(circuitBreaker.run(any(Supplier.class))).thenAnswer(inv ->
                ((Supplier<?>) inv.getArgument(0)).get());
        lenient().when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(inv ->
                ((Supplier<?>) inv.getArgument(0)).get());

        service = new CashService(accountsClient, notificationsClient, circuitBreakerFactory);
    }

    @Test
    void apply_deposit_callsAccountsAndNotifications() {
        service.apply("user1", 500, CashAction.PUT);

        verify(accountsClient).deposit("user1", 500);
        verify(notificationsClient).send(argThat(e ->
                "CASH_IN".equals(e.type()) && e.amount() == 500 && "user1".equals(e.actorLogin())));
    }

    @Test
    void apply_withdraw_callsAccountsAndNotifications() {
        service.apply("user1", 300, CashAction.GET);

        verify(accountsClient).withdraw("user1", 300);
        verify(notificationsClient).send(argThat(e ->
                "CASH_OUT".equals(e.type()) && e.amount() == 300));
    }

    @Test
    void apply_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.apply("user1", 0, CashAction.PUT));
    }

    @Test
    void apply_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.apply("user1", -100, CashAction.GET));
    }
}
