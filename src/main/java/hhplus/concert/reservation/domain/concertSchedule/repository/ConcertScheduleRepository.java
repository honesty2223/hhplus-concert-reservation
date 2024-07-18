package hhplus.concert.reservation.domain.concertSchedule.repository;

import hhplus.concert.reservation.domain.concertSchedule.entity.ConcertSchedule;

import java.util.List;

public interface ConcertScheduleRepository {

    List<ConcertSchedule> findByConcertId(long concertId);

    ConcertSchedule save(ConcertSchedule concertSchedule);
}
