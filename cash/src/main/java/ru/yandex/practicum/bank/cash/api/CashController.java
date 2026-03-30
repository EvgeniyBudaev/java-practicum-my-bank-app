package ru.yandex.practicum.bank.cash.api;

import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.cash.api.dto.CashRequest;
import ru.yandex.practicum.bank.cash.service.CashService;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    private final CashService service;

    public CashController(CashService service) {
        this.service = service;
    }

    private static String extractLogin(JwtAuthenticationToken auth) {
        String preferred = auth.getToken().getClaimAsString("preferred_username");
        return preferred != null ? preferred : auth.getName();
    }

    @PostMapping
    public void cash(@Valid @RequestBody CashRequest req, JwtAuthenticationToken auth) {
        String login = extractLogin(auth);
        service.apply(login, req.value(), req.action());
    }
}
