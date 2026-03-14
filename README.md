# My Bank App

Микросервисное приложение банка на Spring Boot и Spring Cloud.

## Архитектура

| Сервис            | Порт | Назначение                  |
|-------------------|------|-----------------------------|
| **front-ui**      | 8080 | Веб-интерфейс               |
| **gateway**       | 8081 | API Gateway                 |
| **accounts**      | 8082 | Управление аккаунтами       |
| **cash**          | 8083 | Пополнение и снятие средств |
| **transfer**      | 8084 | Переводы между счетами      |
| **notifications** | 8085 | Уведомления (пишет в лог)   |

### Инфраструктура

| Компонент  | Порт | Назначение                   |
|------------|------|------------------------------|
| PostgreSQL | 5432 | БД для сервиса **accounts**  |
| Consul     | 8500 | Service Discovery и Config   |
| Keycloak   | 8088 | Сервер авторизации OAuth 2.0 |

### Взаимодействие

- **Front UI** аутентифицирует пользователя через Keycloak и выполняет запросы в микросервисы через Gateway, пробрасывая
  JWT-токен.
- **Gateway** валидирует JWT, проверяет роли и маршрутизирует запросы к сервисам, обнаруженным через Consul.
- **Микросервисы** (Cash, Transfer, Accounts) авторизуются друг у друга через Keycloak по Client Credentials Flow.
- Межсервисные вызовы обёрнуты в **Circuit Breaker** (Resilience4j).

## Требования к окружению

- Java 21
- Maven 3.9+ (или `mvnw` в комплекте)
- Docker и Docker Compose

## Сборка

```bash
./mvnw clean install
```

# Как запустить

## Локально

Пример с переменными окружения лежит в файле `.env.template` в корне проекта. Для пробного запуска достаточно
переименовать в `.env`.

### 1. Запуск инфраструктуры

```bash
docker compose -f docker-compose.infra.yml up -d
```

### 2. Настройка Keycloak

Конфигурация realm находится в файле `keycloak/bank-realm.json`. Она содержит realm `bank`, клиентов (`front-ui`,
`cash-service`, `transfer-service`, `accounts-service`), роли и тестовых пользователей.

Импорт:

```bash
docker exec bank-keycloak /opt/keycloak/bin/kc.sh import --file /opt/keycloak/data/import/bank-realm.json
```

### 3. Настройка Consul

Добавить общую конфигурацию в Consul KV по ключу `config/application/data`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8088/realms/bank
      client:
        registration:
          front-ui:
            client-id: front-ui
            scope: openid
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
          cash-service:
            client-id: cash-service
            client-secret: <секрет>
            authorization-grant-type: client_credentials
            scope: openid
          transfer-service:
            client-id: transfer-service
            client-secret: <секрет>
            authorization-grant-type: client_credentials
            scope: openid
          accounts-service:
            client-id: accounts-service
            client-secret: <секрет>
            authorization-grant-type: client_credentials
            scope: openid
        provider:
          front-ui:
            issuer-uri: http://localhost:8088/realms/bank
          cash-service:
            issuer-uri: http://localhost:8088/realms/bank
          transfer-service:
            issuer-uri: http://localhost:8088/realms/bank
          accounts-service:
            issuer-uri: http://localhost:8088/realms/bank

bank:
  security:
    enabled: true
  gateway:
    base-url: http://localhost:8081
```

### 4. Запуск сервисов

```bash
java -jar front-ui/target/my-bank-front-app-0.0.1-SNAPSHOT.jar
java -jar gateway/target/my-bank-gateway-0.0.1-SNAPSHOT.jar
java -jar accounts/target/my-bank-accounts-0.0.1-SNAPSHOT.jar
java -jar cash/target/my-bank-cash-0.0.1-SNAPSHOT.jar
java -jar transfer/target/my-bank-transfer-0.0.1-SNAPSHOT.jar
java -jar notifications/target/my-bank-notifications-0.0.1-SNAPSHOT.jar
```

Открыть http://localhost:8080.

## Через Docker Compose

Запуск:

```bash
./mvnw clean package -DskipTests
docker compose up --build -d
```

Остановка:

```bash
docker compose down
```

Realm импортируется автоматически при первом старте.

# Запустить тесты

```bash
./mvnw test
```
