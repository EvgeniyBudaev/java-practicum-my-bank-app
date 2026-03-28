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

/**
 * Контроллер main.html.
 *
 * Используемая модель для main.html:
 *      model.addAttribute("name", name);
 *      model.addAttribute("birthdate", birthdate.format(DateTimeFormatter.ISO_DATE));
 *      model.addAttribute("sum", sum);
 *      model.addAttribute("accounts", accounts);
 *      model.addAttribute("errors", errors);
 *      model.addAttribute("info", info);
 *
 * Поля модели:
 *      name - Фамилия Имя текущего пользователя, String (обязательное)
 *      birthdate - дата рождения текущего пользователя, String в формате 'YYYY-MM-DD' (обязательное)
 *      sum - сумма на счету текущего пользователя, Integer (обязательное)
 *      accounts - список аккаунтов, которым можно перевести деньги, List<AccountDto> (обязательное)
 *      errors - список ошибок после выполнения действий, List<String> (не обязательное)
 *      info - строка успешности после выполнения действия, String (не обязательное)
 *
 */
@Controller
public class MainController {

    private final GatewayApiClient gatewayApiClient;
    private final ObjectMapper objectMapper;

    public MainController(GatewayApiClient gatewayApiClient, ObjectMapper objectMapper) {
        this.gatewayApiClient = gatewayApiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * GET /.
     * Редирект на GET /account
     */
    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    /**
     * GET /account.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для получения данных аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     */
    @GetMapping("/account")
    public String getAccount(Model model, OAuth2AuthenticationToken authentication) {
        try {
            fillModelFromBackend(model, authentication, null, null);
        } catch (Exception e) {
            fillEmptyModel(model, List.of(humanMessage(e)), null);
        }
        return "main";
    }

    /**
     * POST /account.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для изменения данных текущего пользователя по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     *
     * Изменяемые данные:
     * 1. name - Фамилия Имя
     * 2. birthdate - дата рождения в формате YYYY-DD-MM
     */
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

    /**
     * POST /cash.
     * Что нужно сделать:
     * 1. Сходить в сервис cash через Gateway API для снятия/пополнения счета текущего аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     *
     * Параметры:
     * 1. value - сумма списания
     * 2. action - GET (снять), PUT (пополнить)
     */
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

    /**
     * POST /transfer.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для перевода со счета текущего аккаунта на счет другого аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     *
     * Параметры:
     * 1. value - сумма списания
     * 2. login - логин пользователя получателя
     */
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

    /**
     * Заполняет модель данными из бэкенда.
     * <p>
     * Что делает:
     * 1. Получает данные текущего пользователя через API
     * 2. Получает список аккаунтов для перевода
     * 3. Добавляет все данные в модель для отображения на странице
     *
     * @param model модель Spring MVC для передачи данных в шаблон
     * @param authentication токен аутентификации OAuth2 текущего пользователя
     * @param errors список ошибок для отображения (может быть null)
     * @param info сообщение об успешном выполнении операции (может быть null)
     */
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

    /**
     * Безопасное заполнение модели данными из бэкенда.
     * <p>
     * Что делает:
     * 1. Пытается заполнить модель через fillModelFromBackend
     * 2. При возникновении ошибки заполняет модель пустыми значениями
     * 3. Сохраняет сообщения об ошибках для отображения пользователю
     *
     * @param model модель Spring MVC для передачи данных в шаблон
     * @param authentication токен аутентификации OAuth2 текущего пользователя
     * @param errors список ошибок для отображения
     * @param info сообщение об успешном выполнении операции (может быть null)
     */
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

    /**
     * Заполняет модель пустыми/дефолтными значениями.
     * <p>
     * Используется при ошибках получения данных от бэкенда.
     * Устанавливает:
     * - пустое имя
     * - дату рождения (18 лет назад от текущей даты)
     * - сумму 0
     * - пустой список аккаунтов
     *
     * @param model модель Spring MVC для передачи данных в шаблон
     * @param errors список ошибок для отображения
     * @param info сообщение об успешном выполнении операции (может быть null)
     */
    private void fillEmptyModel(Model model, List<String> errors, String info) {
        model.addAttribute("name", "");
        model.addAttribute("birthdate", LocalDate.now().minusYears(18).format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("sum", 0);
        model.addAttribute("accounts", Collections.emptyList());
        model.addAttribute("errors", errors);
        model.addAttribute("info", info);
    }

    /**
     * Получает данные текущего пользователя из сервиса accounts.
     * <p>
     * Делает GET-запрос к /api/accounts/me через Gateway API.
     *
     * @param auth токен аутентификации OAuth2 текущего пользователя
     * @return AccountMeResponse данные аккаунта текущего пользователя (имя, дата рождения, сумма)
     */
    private AccountMeResponse fetchMe(OAuth2AuthenticationToken auth) {
        return gatewayApiClient.get("/api/accounts/me", auth, AccountMeResponse.class);
    }

    /**
     * Получает список аккаунтов для перевода денег.
     * <p>
     * Делает GET-запрос к /api/accounts/recipients через Gateway API.
     * Возвращает список всех доступных аккаунтов, которым можно сделать перевод.
     *
     * @param auth токен аутентификации OAuth2 текущего пользователя
     * @return List<AccountDto> список аккаунтов получателей (пустой список если null)
     */
    private List<AccountDto> fetchRecipients(OAuth2AuthenticationToken auth) {
        AccountDto[] arr = gatewayApiClient.get("/api/accounts/recipients", auth, AccountDto[].class);
        return arr == null ? List.of() : Arrays.asList(arr);
    }

    /**
     * Извлекает список ошибок из исключения RestClientResponseException.
     * <p>
     * Что делает:
     * 1. Парсит тело ответа в формате JSON
     * 2. Пытается получить ошибки из поля errors ответа
     * 3. Если errors пустой, пытается получить сообщение из поля message
     * 4. Если парсинг не удался, возвращает информацию о HTTP статусе
     *
     * @param e исключение RestClientResponseException с телом ответа от сервера
     * @return List<String> список сообщений об ошибках для отображения пользователю
     */
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

    /**
     * Преобразует исключение в человекочитаемое сообщение.
     * <p>
     * Что делает:
     * 1. Для RestClientResponseException возвращает информацию о HTTP статусе
     * 2. Для других исключений возвращает сообщение из исключения или имя класса
     *
     * @param e исключение для преобразования в сообщение
     * @return String человекочитаемое сообщение об ошибке
     */
    private String humanMessage(Exception e) {
        if (e instanceof RestClientResponseException re) {
            return "HTTP %d: %s".formatted(re.getStatusCode().value(), re.getStatusText());
        }
        return Optional.ofNullable(e.getMessage()).orElse(e.getClass().getSimpleName());
    }
}
