package ru.yandex.practicum.bank.accounts.api;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.bank.accounts.api.dto.AccountMeResponse;
import ru.yandex.practicum.bank.accounts.api.dto.EditAccountRequest;
import ru.yandex.practicum.bank.accounts.api.dto.RecipientDto;
import ru.yandex.practicum.bank.accounts.model.Account;
import ru.yandex.practicum.bank.accounts.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountsController {

    private final AccountService service;

    public AccountsController(AccountService service) {
        this.service = service;
    }

    private static String extractLogin(Authentication auth) {
        if (auth == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        if (auth instanceof JwtAuthenticationToken jwt) {
            String preferred = jwt.getToken().getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
        }
        return auth.getName();
    }

    @GetMapping("/me")
    public AccountMeResponse me(Authentication auth) {
        String login = extractLogin(auth);
        Account account = service.getOrCreate(login);
        return new AccountMeResponse(account.getName(), account.getBirthdate(), account.getBalance());
    }

    @PatchMapping("/me")
    public void editMe(@Valid @RequestBody EditAccountRequest req, Authentication auth) {
        String login = extractLogin(auth);
        service.updateProfile(login, req.name(), req.birthdate());
    }

    @GetMapping("/recipients")
    public List<RecipientDto> recipients(Authentication auth) {
        String login = extractLogin(auth);
        return service.recipients(login).stream()
                .map(a -> new RecipientDto(a.getLogin(), a.getName()))
                .toList();
    }
}
