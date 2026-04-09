package ru.yandex.practicum.bank.accounts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.common.dto.NotificationEvent;
import ru.yandex.practicum.bank.accounts.client.NotificationsClient;
import ru.yandex.practicum.bank.accounts.model.Account;
import ru.yandex.practicum.bank.accounts.repo.AccountRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository repo;
    private final NotificationsClient notificationsClient;

    public AccountService(AccountRepository repo,
                          NotificationsClient notificationsClient) {
        this.repo = repo;
        this.notificationsClient = notificationsClient;
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
        try {
            notificationsClient.send(event);
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }
}
