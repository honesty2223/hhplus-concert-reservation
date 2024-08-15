package hhplus.concert.reservation.infrastructure.db;

import hhplus.concert.reservation.domain.outbox.entity.Outbox;
import hhplus.concert.reservation.domain.outbox.entity.OutboxStatus;
import hhplus.concert.reservation.domain.outbox.repository.OutboxRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxRepositoryImpl implements OutboxRepository {
    private final OutboxJpaRepository outboxJpaRepository;

    public OutboxRepositoryImpl(OutboxJpaRepository outboxJpaRepository) {
        this.outboxJpaRepository = outboxJpaRepository;
    }

    @Override
    public List<Outbox> findAllByStatus(OutboxStatus status) {
        return outboxJpaRepository.findAllByStatus(status);
    }

    @Override
    public Outbox findByPaymentId(long paymentId) {
        return outboxJpaRepository.findByPaymentId(paymentId);
    }

    @Override
    public Outbox save(Outbox outbox) {
        return outboxJpaRepository.save(outbox);
    }
}
