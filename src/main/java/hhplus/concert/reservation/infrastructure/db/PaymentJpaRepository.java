package hhplus.concert.reservation.infrastructure.db;

import hhplus.concert.reservation.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Payment findByReservationId(long reservationId);
}
