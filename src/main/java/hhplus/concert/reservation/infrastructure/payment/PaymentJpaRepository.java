package hhplus.concert.reservation.infrastructure.payment;

import hhplus.concert.reservation.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
