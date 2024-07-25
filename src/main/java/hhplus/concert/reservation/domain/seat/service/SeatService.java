package hhplus.concert.reservation.domain.seat.service;

import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import hhplus.concert.reservation.domain.seat.entity.Seat;
import hhplus.concert.reservation.domain.seat.repository.SeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Seat reserveSeatWithLock(long seatId, long customerId) {
        log.info("{}>> [Pessimistic Lock] reserveSeatWithLock 시작, 고객ID: {}", Thread.currentThread().getName(), customerId);
        long startTime = System.currentTimeMillis(); // 시작 시간 기록

        // try 블록 외부에서 선언
        Seat seat = null;
        Seat tempSeat = null;

        try {
            seat = findByIdWithLock(seatId);
            seat.reserveSeat(customerId);
            tempSeat = save(seat);
        } catch (Exception e) {
            log.error("{}>> [Pessimistic Lock] reserveSeatWithLock 예외 발생: 고객ID: {}, 오류 메시지: {}",
                    Thread.currentThread().getName(), customerId, e.getMessage());
            throw e;
        } finally {
            long endTime = System.currentTimeMillis(); // 종료 시간 기록
            long duration = endTime - startTime;
            log.info("{}>> [Pessimistic Lock] reserveSeatWithLock 종료, 소요 시간: {} ms", Thread.currentThread().getName(), duration);
        }
        return tempSeat;
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

    public Seat findByIdWithLock(long seatId) {
        return seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new CoreException(ErrorCode.SEAT_NOT_FOUND));
    }

    public Seat save(Seat seat) {
        return seatRepository.save(seat);
    }
}
