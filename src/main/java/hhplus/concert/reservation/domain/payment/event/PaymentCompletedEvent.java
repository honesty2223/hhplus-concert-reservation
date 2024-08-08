package hhplus.concert.reservation.domain.payment.event;

import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
import lombok.Getter;

@Getter
public class PaymentCompletedEvent {

    private final PaymentDTO paymentDTO;

    public PaymentCompletedEvent(PaymentDTO paymentDTO) {
        this.paymentDTO = paymentDTO;
    }

}
