package hhplus.concert.reservation.infrastructure.db;

import hhplus.concert.reservation.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatJpaRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s WHERE s.concertScheduleId = :concertScheduleId " +
            "AND s.finallyReserved = false " +
            "AND (s.tempAssigneeId IS NULL OR s.tempAssigneeId = 0)")
    List<Seat> findAvailableSeats(@Param("concertScheduleId") long concertScheduleId);

}
