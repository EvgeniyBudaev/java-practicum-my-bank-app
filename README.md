# My Bank App

Микросервисное приложение банка на Spring Boot и Spring Cloud с развертыванием в Kubernetes через Helm.

## Архитектура

| Сервис            | Контейнерный порт | Назначение                                 |
|-------------------|-------------------|--------------------------------------------|
| **front-ui**      | 8080              | Веб-интерфейс                              |
| **gateway**       | 8081              | API Gateway                                |
| **accounts**      | 8082              | Управление аккаунтами                      |
| **cash**          | 8083              | Пополнение и снятие средств                |
| **transfer**      | 8084              | Переводы между счетами                     |
| **notifications** | —                 | Уведомления (Kafka-консьюмер, пишет в лог) |

### Инфраструктура

| Компонент    | Порт внутри кластера | NodePort | Назначение                     |
|--------------|----------------------|----------|--------------------------------|
| PostgreSQL   | 5432                 | —        | БД для **accounts** и Keycloak |
| Keycloak     | 8080                 | 30088    | Сервер авторизации OAuth 2.0   |
| Apache Kafka | 9092                 | —        | Брокер сообщений               |

### Взаимодействие

- **Front UI** аутентифицирует пользователя через Keycloak и выполняет запросы в микросервисы через Gateway, пробрасывая
  JWT-токен.
- **Gateway** валидирует JWT, проверяет роли и маршрутизирует запросы к сервисам Accounts, Cash и Transfer через K8s
  DNS (Service Discovery).
- **Cash** и **Transfer** авторизуются в Accounts через Keycloak по Client Credentials Flow. Межсервисные REST-вызовы
  обёрнуты в Circuit Breaker (Resilience4j).
- **Accounts**, **Cash** и **Transfer** отправляют уведомления в топик `bank.notifications` через Apache Kafka.
- **Notifications** читает сообщения из топика `bank.notifications` (Kafka Consumer) и логирует уведомления.
- Конфигурация хранится в **ConfigMaps** и **Secrets** Kubernetes.

## Требования к окружению

- Java 21
- Maven 3.9+ (или `mvnw` в комплекте)
- Docker
- Minikube
- kubectl
- Helm 4

## Сборка

### 1. Сборка JAR-файлов

```bash
./mvnw clean package -DskipTests
```

### 2. Сборка Docker-образов

```bash
docker build -t my-bank-app/front-ui:latest ./front-ui
docker build -t my-bank-app/gateway:latest ./gateway
docker build -t my-bank-app/accounts:latest ./accounts
docker build -t my-bank-app/cash:latest ./cash
docker build -t my-bank-app/transfer:latest ./transfer
docker build -t my-bank-app/notifications:latest ./notifications
```

### 3. Запуск Minikube и загрузка образов

```bash
minikube start --driver=docker --ports=30080:30080 --ports=30081:30081 --ports=30088:30088
minikube image load my-bank-app/front-ui:latest
minikube image load my-bank-app/gateway:latest
minikube image load my-bank-app/accounts:latest
minikube image load my-bank-app/cash:latest
minikube image load my-bank-app/transfer:latest
minikube image load my-bank-app/notifications:latest
```

## Развертывание в Kubernetes (Helm)

### Сборка зависимостей и установка

```bash
helm dependency build helm/my-bank-app/
helm install my-bank helm/my-bank-app/
```

### С переопределением для конкретной среды

```bash
helm dependency build helm/my-bank-app/

helm install my-bank helm/my-bank-app/ -f helm/my-bank-app/values-dev.yaml -n dev --create-namespace

helm install my-bank helm/my-bank-app/ -f helm/my-bank-app/values-prod.yaml -n prod --create-namespace
```

При установке Kafka-сабчарт автоматически создаёт StatefulSet с брокером и Job для создания топика `bank.notifications`.

### Удаление

```bash
helm uninstall my-bank
```

## Доступ к приложению

| Компонент | URL                    | Назначение             |
|-----------|------------------------|------------------------|
| Front UI  | http://localhost:30080 | Веб-интерфейс          |
| Gateway   | http://localhost:30081 | API Gateway            |
| Keycloak  | http://localhost:30088 | Админ-панель OAuth 2.0 |

## Запустить тесты

### Java

```bash
./mvnw test
```

### Helm

```bash
helm test my-bank
```
