package ru.yandex.practicum.bank.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import ru.yandex.practicum.bank.transfer.client.AccountsClient;
import ru.yandex.practicum.bank.transfer.client.NotificationsClient;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private NotificationsClient notificationsClient;

    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Mock
    private CircuitBreaker circuitBreaker;

    private TransferService service;

    @BeforeEach
    void setUp() {
        lenient().when(circuitBreakerFactory.create(anyString())).thenReturn(circuitBreaker);
        lenient().when(circuitBreaker.run(any(Supplier.class))).thenAnswer(inv ->
                ((Supplier<?>) inv.getArgument(0)).get());
        lenient().when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(inv ->
                ((Supplier<?>) inv.getArgument(0)).get());

        service = new TransferService(accountsClient, notificationsClient, circuitBreakerFactory);
    }

    @Test
    void transfer_success() {
        service.transfer("sender", "receiver", 500);

        InOrder inOrder = inOrder(accountsClient, notificationsClient);
        inOrder.verify(accountsClient).withdraw("sender", 500);
        inOrder.verify(accountsClient).deposit("receiver", 500);
        inOrder.verify(notificationsClient).send(argThat(e ->
                "TRANSFER".equals(e.type()) && e.amount() == 500
                        && "sender".equals(e.actorLogin()) && "receiver".equals(e.targetLogin())));
    }

    @Test
    void transfer_depositFails_rollsBackWithdrawal() {
        doNothing().when(accountsClient).withdraw("sender", 500);
        doThrow(new RuntimeException("deposit failed")).when(accountsClient).deposit("receiver", 500);

        assertThrows(RuntimeException.class,
                () -> service.transfer("sender", "receiver", 500));

        verify(accountsClient).deposit("sender", 500);
    }

    @Test
    void transfer_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("sender", "receiver", 0));
    }

    @Test
    void transfer_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("sender", "receiver", -100));
    }

    @Test
    void transfer_blankRecipient_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("sender", "", 500));
    }

    @Test
    void transfer_nullRecipient_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("sender", null, 500));
    }

    @Test
    void transfer_selfTransfer_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("sender", "sender", 500));
    }
}
