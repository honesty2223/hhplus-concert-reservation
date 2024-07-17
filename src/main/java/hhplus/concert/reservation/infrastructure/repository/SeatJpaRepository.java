package hhplus.concert.reservation.infrastructure.repository;

import hhplus.concert.reservation.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatJpaRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s WHERE s.concertScheduleId = :concertScheduleId " +
            "AND s.finallyReserved = false " +
            "AND (s.tempAssigneeId IS NULL OR s.tempAssigneeId = 0)")
    List<Seat> findAvailableSeats(@Param("concertScheduleId") long concertScheduleId);

}
