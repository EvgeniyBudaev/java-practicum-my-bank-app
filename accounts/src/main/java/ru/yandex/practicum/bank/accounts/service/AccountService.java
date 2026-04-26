package ru.yandex.practicum.bank.accounts.service;

import io.micrometer.core.instrument.MeterRegistry;
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
    private final MeterRegistry meterRegistry;

    public AccountService(AccountRepository repo,
                          NotificationsClient notificationsClient,
                          MeterRegistry meterRegistry) {
        this.repo = repo;
        this.notificationsClient = notificationsClient;
        this.meterRegistry = meterRegistry;
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
            log.info("Creating new account for login={}", login);
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
        log.info("Updating profile: login={}, name={}", login, name);
        Account account = getOrCreate(login);
        account.setName(name);
        account.setBirthdate(birthdate);
        Account saved = repo.save(account);
        log.info("Profile updated successfully: login={}", login);
        sendNotification(new NotificationEvent(
                "PROFILE_UPDATE", 0, login, null, OffsetDateTime.now()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Account> recipients(String login) {
        log.debug("Fetching recipients for login={}", login);
        return repo.findAllRecipients(login);
    }

    @Transactional
    public void deposit(String login, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }
        log.info("Deposit: login={}, amount={}", login, amount);
        getOrCreate(login);
        int updated = repo.deposit(login, amount);
        if (updated != 1) {
            log.error("Deposit failed — unexpected row count: login={}, amount={}, rows={}", login, amount, updated);
            throw new IllegalStateException("Не удалось пополнить баланс");
        }
        log.info("Deposit successful: login={}, amount={}", login, amount);
    }

    @Transactional
    public void withdraw(String login, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }
        log.info("Withdrawal request: login={}, amount={}", login, amount);
        getOrCreate(login);
        int updated = repo.withdrawIfEnough(login, amount);
        if (updated != 1) {
            log.warn("Withdrawal failed — insufficient funds: login={}, amount={}", login, amount);
            meterRegistry.counter("bank.cash.withdrawal.failed").increment();
            throw new NotEnoughFundsException("Недостаточно средств на счету");
        }
        log.info("Withdrawal successful: login={}, amount={}", login, amount);
    }

    private void sendNotification(NotificationEvent event) {
        try {
            notificationsClient.send(event);
        } catch (Exception e) {
            log.warn("Failed to send notification: type={}, login={}, error={}",
                    event.type(), event.actorLogin(), e.getMessage());
        }
    }
}
