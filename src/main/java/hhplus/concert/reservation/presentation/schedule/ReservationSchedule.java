package hhplus.concert.reservation.presentation.schedule;

import hhplus.concert.reservation.application.reservation.usecase.ReservationUsecase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ReservationSchedule {

    @Autowired
    private ReservationUsecase reservationUsecase;

    // 30초마다 만료된 예약을 취소합니다
    @Scheduled(fixedRate = 30000)
    public void cancelExpiredReservations() {
        reservationUsecase.cancelExpiredReservations();
    }

}
