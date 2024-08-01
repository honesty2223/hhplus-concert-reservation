package hhplus.concert.reservation.presentation.controller.reservation.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private long customerId;
    private long concertId;
    private long reservationId;
    private long amount;
}
