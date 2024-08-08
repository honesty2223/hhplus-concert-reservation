package hhplus.concert.reservation.infrastructure;

import hhplus.concert.reservation.domain.reservation.entity.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    // 결제 대기 상태 and 예약 시간이 기준 시간 이전인 예약을 찾는 메소드
    List<Reservation> findByStatusAndReservationTimeBefore(String status, LocalDateTime reservationTime);
}
