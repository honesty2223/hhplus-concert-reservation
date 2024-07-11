package hhplus.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private long paymentId;

    @Column(name = "customer_id")
    private long customerId;

    @Column(name = "reservation_id")
    private long reservationId;

    @Column(name = "amount")
    private long amount;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime = LocalDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Payment(long customerId, long reservationId, long amount) {
        this.customerId = customerId;
        this.reservationId = reservationId;
        this.amount = amount;
    }

}
