package ru.yandex.practicum.bank.cash.client;

public interface AccountsClient {
    void deposit(String login, long amount);

    void withdraw(String login, long amount);
}
