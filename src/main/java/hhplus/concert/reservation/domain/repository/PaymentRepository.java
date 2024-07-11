package hhplus.concert.reservation.domain.repository;

import hhplus.concert.reservation.domain.entity.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findById(long paymentId);

    Payment save(Payment payment);

}
