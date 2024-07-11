package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.entity.Reservation;
import hhplus.concert.reservation.domain.repository.ReservationRepository;
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
                .orElseThrow(() -> new RuntimeException("해당 예약을 찾을 수 없습니다 : " + reservationId));
    }

    public void cancelExpiredReservations() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);

        // 'PENDING' 상태의 예약 중 예약 시간이 5분이 지난 예약을 찾습니다.
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndReservationTimeBefore("PENDING", cutoffTime);

        // 예약 취소
        for (Reservation reservation : expiredReservations) {
            reservation.cancel();
            reservationRepository.save(reservation);
        }
    }

    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
}
