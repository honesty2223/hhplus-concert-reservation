package hhplus.concert.reservation.presentation.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.concert.reservation.domain.outbox.entity.Outbox;
import hhplus.concert.reservation.domain.outbox.repository.OutboxRepository;
import hhplus.concert.reservation.domain.payment.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class PaymentCompletedEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    public PaymentCompletedEventListener(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, OutboxRepository outboxRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveOutbox(PaymentCompletedEvent event) {
        try {
            log.info("saveOutbox 실행");
            String message = objectMapper.writeValueAsString(event);
            Outbox outbox = new Outbox(event.getPaymentDTO().getPaymentId(), message);
            outboxRepository.save(outbox);
            log.info("outbox : {}", outbox);
        } catch (Exception e) {
            log.error("Outbox 저장 실패: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        try {
            log.info("handlePaymentCompletedEvent 실행");
            String message = objectMapper.writeValueAsString(event);
            // Kafka 에 전송
            kafkaTemplate.send("payment-topic", message);
            log.info("message : {}", message);
        } catch (Exception e) {
            log.error("Kafka 전송 실패: {}", e.getMessage());
        }
    }
}
