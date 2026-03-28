package ru.yandex.practicum.bank.transfer.api;

import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.transfer.api.dto.TransferRequest;
import ru.yandex.practicum.bank.transfer.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    private static String extractLogin(JwtAuthenticationToken auth) {
        String preferred = auth.getToken().getClaimAsString("preferred_username");
        return preferred != null ? preferred : auth.getName();
    }

    @PostMapping
    public void transfer(@Valid @RequestBody TransferRequest req, JwtAuthenticationToken auth) {
        String fromLogin = extractLogin(auth);
        service.transfer(fromLogin, req.login(), req.value());
    }
}
