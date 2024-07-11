package hhplus.concert.reservation.infrastructure.repository;

import hhplus.concert.reservation.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
    // 결제 대기 상태 and 예약 시간이 기준 시간 이전인 예약을 찾는 메소드
    List<Reservation> findByStatusAndReservationTimeBefore(String status, LocalDateTime reservationTime);
}
