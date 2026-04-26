package ru.yandex.practicum.bank.notifications.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.common.dto.NotificationEvent;

@Slf4j
@Component
public class NotificationListener {

    private final MeterRegistry meterRegistry;

    public NotificationListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(
            topics = "${bank.kafka.topic.notifications}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onNotification(NotificationEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Processing notification: type={}, amount={}, actor={}, target={}, at={}",
                    event.type(), event.amount(), event.actorLogin(),
                    event.targetLogin(), event.occurredAt());

            acknowledgment.acknowledge();

            log.debug("Notification acknowledged: type={}, actor={}", event.type(), event.actorLogin());
        } catch (Exception e) {
            String login = event.actorLogin() != null ? event.actorLogin() : "unknown";
            log.error("Failed to process notification: type={}, actor={}, error={}",
                    event.type(), login, e.getMessage(), e);
            meterRegistry.counter("bank.notification.send.failed", "type", event.type()).increment();
        }
    }
}
