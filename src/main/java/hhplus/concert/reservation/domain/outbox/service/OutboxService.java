package hhplus.concert.reservation.domain.outbox.service;

import hhplus.concert.reservation.domain.outbox.entity.Outbox;
import hhplus.concert.reservation.domain.outbox.entity.OutboxStatus;
import hhplus.concert.reservation.domain.outbox.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxService(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 10000)
    public void outboxProcess() {
        log.info("outboxProcess 실행");
        List<Outbox> outboxes = outboxRepository.findAllByStatus(OutboxStatus.INIT);

        for (Outbox outbox : outboxes) {
            try {
                String message = outbox.getMessage();
                // Kafka 에 전송
                kafkaTemplate.send("payment-topic", message);
                log.info("스케줄러 Kafka 전송 성공 : {}", message);
            } catch (Exception e) {
                log.error("스케줄러 Kafka 전송 실패: {}", e.getMessage());
            }
        }
    }
}
