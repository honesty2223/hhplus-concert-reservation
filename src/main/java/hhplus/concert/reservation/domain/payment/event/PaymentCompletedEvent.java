package hhplus.concert.reservation.domain.payment.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
import lombok.Getter;

@Getter
public class PaymentCompletedEvent {

    private final PaymentDTO paymentDTO;

    @JsonCreator
    public PaymentCompletedEvent(@JsonProperty("paymentDTO") PaymentDTO paymentDTO) {
        this.paymentDTO = paymentDTO;
    }

}
