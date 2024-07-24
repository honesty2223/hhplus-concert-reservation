package hhplus.concert.reservation.domain.customer.entity;

import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private long customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "point", nullable = false)
    private long point = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 포인트 충전
    public void chargePoint(long amount) {
        this.point += amount;
    }

    // 포인트 차감
    public void deductPoint(long amount) {
        // 포인트가 부족한 경우 예외를 발생시킵니다
        if (amount > this.point) {
            throw new CoreException(ErrorCode.INSUFFICIENT_POINTS);
        }

        this.point -= amount;
    }

}
