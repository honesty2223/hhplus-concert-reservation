package hhplus.concert.reservation.domain.seat.service;

import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import hhplus.concert.reservation.domain.seat.entity.Seat;
import hhplus.concert.reservation.domain.seat.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> findAvailableSeats(long concertScheduleId) {
        List<Seat> seats = seatRepository.findAvailableSeats(concertScheduleId);
        if (seats.isEmpty()) {
            throw new CoreException(ErrorCode.NO_AVAILABLE_SEATS);
        }
        return seats;
    }

    public Seat findById(long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new CoreException(ErrorCode.SEAT_NOT_FOUND));
    }

    public Seat save(Seat seat) {
        return seatRepository.save(seat);
    }
}
