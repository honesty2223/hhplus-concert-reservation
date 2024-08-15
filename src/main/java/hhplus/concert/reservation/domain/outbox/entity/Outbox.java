package hhplus.concert.reservation.domain.outbox.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "payment_id")
    private long paymentId;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private String message;

    public Outbox(long paymentId, String message) {
        this.paymentId = paymentId;
        this.status = OutboxStatus.INIT;
        this.message = message;
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
    }

}
