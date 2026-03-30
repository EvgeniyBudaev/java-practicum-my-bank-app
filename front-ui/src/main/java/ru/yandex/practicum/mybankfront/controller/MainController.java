package ru.yandex.practicum.mybankfront.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.mybankfront.client.GatewayApiClient;
import ru.yandex.practicum.mybankfront.client.dto.*;
import ru.yandex.practicum.mybankfront.controller.dto.AccountDto;
import ru.yandex.practicum.mybankfront.controller.dto.CashAction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {

    private final GatewayApiClient gatewayApiClient;
    private final ObjectMapper objectMapper;

    public MainController(GatewayApiClient gatewayApiClient, ObjectMapper objectMapper) {
        this.gatewayApiClient = gatewayApiClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(Model model, OAuth2AuthenticationToken authentication) {
        try {
            fillModelFromBackend(model, authentication, null, null);
        } catch (Exception e) {
            fillEmptyModel(model, List.of(humanMessage(e)), null);
        }
        return "main";
    }

    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam("name") String name,
            @RequestParam("birthdate") LocalDate birthdate,
            OAuth2AuthenticationToken authentication
    ) {
        try {
            gatewayApiClient.patchJson("/api/accounts/me", new EditAccountRequest(name, birthdate), authentication);
            fillModelFromBackend(model, authentication, null, "Изменения сохранены");
        } catch (RestClientResponseException e) {
            fillModelFromBackendSafe(model, authentication, extractErrors(e), null);
        } catch (Exception e) {
            fillModelFromBackendSafe(model, authentication, List.of(humanMessage(e)), null);
        }
        return "main";
    }

    @PostMapping("/cash")
    public String editCash(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("action") CashAction action,
            OAuth2AuthenticationToken authentication
    ) {
        try {
            gatewayApiClient.postJson("/api/cash", new CashRequest(value, action), authentication, Void.class);

            String info = action == CashAction.GET
                    ? "Снято %d руб".formatted(value)
                    : "Положено %d руб".formatted(value);

            fillModelFromBackend(model, authentication, null, info);
        } catch (RestClientResponseException e) {
            fillModelFromBackendSafe(model, authentication, extractErrors(e), null);
        } catch (Exception e) {
            fillModelFromBackendSafe(model, authentication, List.of(humanMessage(e)), null);
        }
        return "main";
    }

    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("login") String login,
            OAuth2AuthenticationToken authentication
    ) {
        try {
            List<AccountDto> recipients = fetchRecipients(authentication);
            String recipientName = recipients.stream()
                    .filter(a -> a.login().equals(login))
                    .map(AccountDto::name)
                    .findFirst()
                    .orElse(login);

            gatewayApiClient.postJson("/api/transfers", new TransferRequest(value, login), authentication, Void.class);

            String info = "Успешно переведено %d руб клиенту %s".formatted(value, recipientName);
            fillModelFromBackend(model, authentication, null, info);
        } catch (RestClientResponseException e) {
            fillModelFromBackendSafe(model, authentication, extractErrors(e), null);
        } catch (Exception e) {
            fillModelFromBackendSafe(model, authentication, List.of(humanMessage(e)), null);
        }
        return "main";
    }

    private void fillModelFromBackend(Model model,
                                      OAuth2AuthenticationToken authentication,
                                      List<String> errors,
                                      String info) {
        AccountMeResponse me = fetchMe(authentication);
        List<AccountDto> recipients = fetchRecipients(authentication);

        model.addAttribute("name", me.name());
        model.addAttribute("birthdate", me.birthdate().format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("sum", me.sum());
        model.addAttribute("accounts", recipients);

        model.addAttribute("errors", errors);
        model.addAttribute("info", info);
    }

    private void fillModelFromBackendSafe(Model model,
                                          OAuth2AuthenticationToken authentication,
                                          List<String> errors,
                                          String info) {
        try {
            fillModelFromBackend(model, authentication, errors, info);
        } catch (Exception e) {
            fillEmptyModel(model, errors != null ? errors : List.of(humanMessage(e)), info);
        }
    }

    private void fillEmptyModel(Model model, List<String> errors, String info) {
        model.addAttribute("name", "");
        model.addAttribute("birthdate", LocalDate.now().minusYears(18).format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("sum", 0);
        model.addAttribute("accounts", Collections.emptyList());
        model.addAttribute("errors", errors);
        model.addAttribute("info", info);
    }

    private AccountMeResponse fetchMe(OAuth2AuthenticationToken auth) {
        return gatewayApiClient.get("/api/accounts/me", auth, AccountMeResponse.class);
    }

    private List<AccountDto> fetchRecipients(OAuth2AuthenticationToken auth) {
        AccountDto[] arr = gatewayApiClient.get("/api/accounts/recipients", auth, AccountDto[].class);
        return arr == null ? List.of() : Arrays.asList(arr);
    }

    private List<String> extractErrors(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            try {
                ApiErrorResponse parsed = objectMapper.readValue(body, ApiErrorResponse.class);
                if (parsed.errors() != null && !parsed.errors().isEmpty()) {
                    return parsed.errors();
                }
                if (parsed.message() != null && !parsed.message().isBlank()) {
                    return List.of(parsed.message());
                }
            } catch (Exception ignore) {
            }
        }
        return List.of("HTTP %d: %s".formatted(e.getStatusCode().value(), e.getStatusText()));
    }

    private String humanMessage(Exception e) {
        if (e instanceof RestClientResponseException re) {
            return "HTTP %d: %s".formatted(re.getStatusCode().value(), re.getStatusText());
        }
        return Optional.ofNullable(e.getMessage()).orElse(e.getClass().getSimpleName());
    }
}
