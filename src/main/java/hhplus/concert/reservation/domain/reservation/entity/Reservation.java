package hhplus.concert.reservation.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private long reservationId;

    @Column(name = "customer_id")
    private long customerId;

    @Column(name = "seat_id")
    private long seatId;

    @Column(name = "concert_schedule_id")
    private long concertScheduleId;

    @Column(name = "reservation_time")
    private LocalDateTime reservationTime = LocalDateTime.now();

    @Column(name = "status")
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private long version;

    // version 제외한 생성자
    public Reservation(long reservationId, long customerId, long seatId, long concertScheduleId,LocalDateTime reservationTime, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.seatId = seatId;
        this.reservationTime = reservationTime;
        this.status = status;
        this.concertScheduleId = concertScheduleId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Reservation(long customerId, long seatId, long concertScheduleId) {
        this.customerId = customerId;
        this.seatId = seatId;
        this.concertScheduleId = concertScheduleId;
    }

    // 예약 취소
    public void cancel() {
        this.status = "CANCELLED";
    }

    // 결제 완료
    public void completed() {
        this.status = "COMPLETED";
    }

}
