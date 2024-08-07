package hhplus.concert.reservation.domain.payment.repository;

import hhplus.concert.reservation.domain.payment.entity.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findById(long paymentId);

    Payment findByReservationId(long reservationId);

    Payment save(Payment payment);

}
