package ru.yandex.practicum.bank.cash.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bank.kafka.topic")
public class KafkaTopicProperties {

    private String notifications;

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }
}
