package hhplus.concert.reservation.presentation.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.concert.reservation.domain.outbox.entity.Outbox;
import hhplus.concert.reservation.domain.outbox.repository.OutboxRepository;
import hhplus.concert.reservation.domain.payment.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumer {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumer(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment-topic", groupId = "payment-group")
    public void consume(String message) {
        try {
            log.info("consume 실행");
            // 메시지 파싱
            PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);

            // Outbox 업데이트
            log.info("PaymentId: {}", event.getPaymentDTO().getPaymentId());
            Outbox outbox = outboxRepository.findByPaymentId(event.getPaymentDTO().getPaymentId());
            log.info("변경 전 Outbox Status: {}", outbox.getStatus());
            outbox.markAsPublished();
            outboxRepository.save(outbox);
            log.info("변경 후 Outbox Status: {}", outbox.getStatus());

            log.info("메시지 처리 완료: {}", event);
        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", e.getMessage());
        }
    }
}
