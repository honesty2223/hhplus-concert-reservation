package hhplus.concert.reservation.infrastructure.repository;

import hhplus.concert.reservation.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
