package hhplus.concert.reservation.domain.reservation.service;

import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import hhplus.concert.reservation.domain.reservation.entity.Reservation;
import hhplus.concert.reservation.domain.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation findById(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CoreException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public Reservation findByIdWithLock(long reservationId) {
        return reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new CoreException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public List<Reservation> cancelExpiredReservations() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);

        // 'PENDING' 상태의 예약 중 예약 시간이 5분이 지난 예약을 찾음
        return reservationRepository.findByStatusAndReservationTimeBefore("PENDING", cutoffTime);
    }

    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
}
