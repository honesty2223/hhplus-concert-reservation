package hhplus.concert.reservation.infrastructure;

import hhplus.concert.reservation.domain.payment.entity.Payment;
import hhplus.concert.reservation.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Optional<Payment> findById(long paymentId) {
        return paymentJpaRepository.findById(paymentId);
    }

    @Override
    public Payment findByReservationId(long reservationId) {
        return paymentJpaRepository.findByReservationId(reservationId);
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }
}
