package hhplus.concert.reservation.infrastructure.concertSchedule;

import hhplus.concert.reservation.domain.concertSchedule.entity.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {

    List<ConcertSchedule> findByConcertId(long concertId);

}
