package ru.yandex.practicum.bank.accounts.service;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.accounts.client.NotificationEvent;
import ru.yandex.practicum.bank.accounts.client.NotificationsClient;
import ru.yandex.practicum.bank.accounts.model.Account;
import ru.yandex.practicum.bank.accounts.repo.AccountRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository repo;
    private final NotificationsClient notificationsClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public AccountService(AccountRepository repo,
                          NotificationsClient notificationsClient,
                          CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.repo = repo;
        this.notificationsClient = notificationsClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    private static void validateAdult(LocalDate birthdate) {
        if (birthdate == null) {
            throw new IllegalArgumentException("Дата рождения обязательна");
        }
        int years = Period.between(birthdate, LocalDate.now()).getYears();
        if (years < 18) {
            throw new IllegalArgumentException("Возраст должен быть больше 18 лет");
        }
    }

    @Transactional
    public Account getOrCreate(String login) {
        return repo.findById(login).orElseGet(() -> {
            Account created = new Account(
                    login,
                    login,
                    LocalDate.now().minusYears(18),
                    0L
            );
            return repo.save(created);
        });
    }

    @Transactional
    public Account updateProfile(String login, String name, LocalDate birthdate) {
        validateAdult(birthdate);

        Account account = getOrCreate(login);
        account.setName(name);
        account.setBirthdate(birthdate);
        Account saved = repo.save(account);
        sendNotification(new NotificationEvent(
                "PROFILE_UPDATE", 0, login, null, OffsetDateTime.now()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Account> recipients(String login) {
        return repo.findAllRecipients(login);
    }

    @Transactional
    public void deposit(String login, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }
        getOrCreate(login);
        int updated = repo.deposit(login, amount);
        if (updated != 1) {
            throw new IllegalStateException("Не удалось пополнить баланс");
        }
    }

    @Transactional
    public void withdraw(String login, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }
        getOrCreate(login);
        int updated = repo.withdrawIfEnough(login, amount);
        if (updated != 1) {
            throw new NotEnoughFundsException("Недостаточно средств на счету");
        }
    }

    private void sendNotification(NotificationEvent event) {
        circuitBreakerFactory.create("notifications").run(
                () -> {
                    notificationsClient.send(event);
                    return null;
                },
                throwable -> null);
    }
}
