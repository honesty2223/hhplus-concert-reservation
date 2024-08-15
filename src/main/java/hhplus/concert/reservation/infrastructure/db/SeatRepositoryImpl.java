package hhplus.concert.reservation.infrastructure.db;

import hhplus.concert.reservation.domain.seat.entity.Seat;
import hhplus.concert.reservation.domain.seat.repository.SeatRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SeatRepositoryImpl implements SeatRepository {
    private final SeatJpaRepository seatJpaRepository;

    public SeatRepositoryImpl(SeatJpaRepository seatJpaRepository) {
        this.seatJpaRepository = seatJpaRepository;
    }

    @Override
    public List<Seat> findAvailableSeats(long concertScheduleId) {
        return seatJpaRepository.findAvailableSeats(concertScheduleId);
    }

    @Override
    public Optional<Seat> findById(long seatId) {
        return seatJpaRepository.findById(seatId);
    }

    @Override
    public Seat save(Seat seat) {
        return seatJpaRepository.save(seat);
    }
}
