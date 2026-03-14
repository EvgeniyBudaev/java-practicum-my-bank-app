package ru.yandex.practicum.bank.accounts.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("accounts")
public class Account {

    @Id
    private String login;

    private String name;
    private LocalDate birthdate;
    private long balance;

    public Account() {
    }

    public Account(String login, String name, LocalDate birthdate, long balance) {
        this.login = login;
        this.name = name;
        this.birthdate = birthdate;
        this.balance = balance;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
