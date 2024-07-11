package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.entity.Seat;
import hhplus.concert.reservation.domain.repository.SeatRepository;
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
            throw new RuntimeException("해당 날짜에 예약 가능한 좌석이 없습니다.");
        }
        return seats;
    }

    public Seat findById(long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("해당 좌석을 찾을 수 없습니다 : " + seatId));
    }

    public Seat save(Seat seat) {
        return seatRepository.save(seat);
    }
}
