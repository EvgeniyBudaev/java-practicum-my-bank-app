package ru.yandex.practicum.bank.accounts.repo;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.yandex.practicum.bank.accounts.model.Account;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, String> {

    @Query("select * from accounts where login <> :login order by login")
    List<Account> findAllRecipients(String login);

    @Modifying
    @Query("update accounts set balance = balance + :amount where login = :login")
    int deposit(String login, long amount);

    @Modifying
    @Query("update accounts set balance = balance - :amount where login = :login and balance >= :amount")
    int withdrawIfEnough(String login, long amount);
}
