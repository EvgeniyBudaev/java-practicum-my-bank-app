package ru.yandex.practicum.bank.transfer.client;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.common.dto.NotificationEvent;

@Component
public class KafkaNotificationsClient implements NotificationsClient {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final KafkaTopicProperties kafkaTopicProperties;

    public KafkaNotificationsClient(KafkaTemplate<String, NotificationEvent> kafkaTemplate,
                                    KafkaTopicProperties kafkaTopicProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicProperties = kafkaTopicProperties;
    }

    @Override
    public void send(NotificationEvent event) {
        kafkaTemplate.send(kafkaTopicProperties.getNotifications(), event.actorLogin(), event);
    }
}
