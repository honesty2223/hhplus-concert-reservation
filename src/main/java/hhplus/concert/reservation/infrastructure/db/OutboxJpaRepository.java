package hhplus.concert.reservation.infrastructure.db;

import hhplus.concert.reservation.domain.outbox.entity.Outbox;
import hhplus.concert.reservation.domain.outbox.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxJpaRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findAllByStatus(OutboxStatus status);
    Outbox findByPaymentId(long paymentId);
}
