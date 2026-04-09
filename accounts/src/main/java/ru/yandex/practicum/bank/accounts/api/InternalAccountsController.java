package ru.yandex.practicum.bank.accounts.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.bank.accounts.api.dto.BalanceChangeRequest;
import ru.yandex.practicum.bank.accounts.service.AccountService;

@RestController
@RequestMapping("/internal/accounts")
public class InternalAccountsController {

    private final AccountService service;

    public InternalAccountsController(AccountService service) {
        this.service = service;
    }

    @PostMapping("/{login}/deposit")
    public void deposit(@PathVariable String login, @Valid @RequestBody BalanceChangeRequest req) {
        service.deposit(login, req.amount());
    }

    @PostMapping("/{login}/withdraw")
    public void withdraw(@PathVariable String login, @Valid @RequestBody BalanceChangeRequest req) {
        service.withdraw(login, req.amount());
    }
}
