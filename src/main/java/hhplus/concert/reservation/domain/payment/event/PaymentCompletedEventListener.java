package hhplus.concert.reservation.domain.payment.event;

import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class PaymentCompletedEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 처리를 시작합니다. PaymentDTO: {}", event.getPaymentDTO());
        RestTemplate restTemplate = new RestTemplate();

        PaymentDTO paymentDTO = event.getPaymentDTO();

        // 외부 API에 결제 정보 전송
        String externalApiUrl = "http://localhost:8080/api/payment";  // Mock API URL

        try {
            restTemplate.postForEntity(externalApiUrl, paymentDTO, PaymentDTO.class);
            log.info("결제 정보 외부 API로 전송 성공");
        } catch (Exception e) {
            log.error("결제 정보 외부 API 전송 실패: {}", e.getMessage());
        }
    }
}
