package hhplus.concert.reservation.domain.outbox.repository;

import hhplus.concert.reservation.domain.outbox.entity.Outbox;
import hhplus.concert.reservation.domain.outbox.entity.OutboxStatus;

import java.util.List;

public interface OutboxRepository {

    List<Outbox> findAllByStatus(OutboxStatus status);

    Outbox findByPaymentId(long paymentId);

    Outbox save(Outbox outbox);
}
