package hhplus.concert.reservation.domain.repository;

import hhplus.concert.reservation.domain.entity.ConcertSchedule;

import java.util.List;

public interface ConcertScheduleRepository {

    List<ConcertSchedule> findByConcertId(long concertId);

    ConcertSchedule save(ConcertSchedule concertSchedule);
}
