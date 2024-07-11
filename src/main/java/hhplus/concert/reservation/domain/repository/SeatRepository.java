package hhplus.concert.reservation.domain.repository;

import hhplus.concert.reservation.domain.entity.Seat;

import java.util.List;
import java.util.Optional;

public interface SeatRepository {

    List<Seat> findAvailableSeats(long concertScheduleId);

    Optional<Seat> findById(long seatId);

    Seat save(Seat seat);
}
