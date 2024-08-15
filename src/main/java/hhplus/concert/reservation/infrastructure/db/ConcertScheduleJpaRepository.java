package hhplus.concert.reservation.infrastructure.db;

import hhplus.concert.reservation.domain.concertSchedule.entity.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {

    List<ConcertSchedule> findByConcertId(long concertId);

}
