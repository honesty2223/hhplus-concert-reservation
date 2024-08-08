package hhplus.concert.reservation.application.reservation.usecase;

import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
import hhplus.concert.reservation.application.reservation.dto.ReservationDTO;
import hhplus.concert.reservation.domain.reservation.entity.Reservation;
import hhplus.concert.reservation.domain.reservation.service.ReservationService;
import hhplus.concert.reservation.domain.seat.entity.Seat;
import hhplus.concert.reservation.domain.seat.service.SeatService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationUsecase {

    private final ReservationManager reservationManager;
    private final SeatService seatService;
    private final ReservationService reservationService;

    public ReservationUsecase(ReservationManager reservationManager, SeatService seatService, ReservationService reservationService) {
        this.reservationManager = reservationManager;
        this.seatService = seatService;
        this.reservationService = reservationService;
    }

    /**
     * 좌석 예약 요청
     *
     * @return 예약 정보를 담은 ReservationDTO 객체 리스트
     */
    public ReservationDTO createReservation(long seatId, long customerId) {
        Seat seat = seatService.reserveSeatWithLock(seatId, customerId);

        Reservation reservation = new Reservation(customerId, seat.getSeatId(), seat.getConcertScheduleId());
        Reservation savedReservation = reservationService.save(reservation);

        return new ReservationDTO(savedReservation.getReservationId(), savedReservation.getCustomerId(), savedReservation.getSeatId(), savedReservation.getConcertScheduleId(), savedReservation.getReservationTime(), savedReservation.getStatus());
    }

    /**
     * 결제 처리 및 결제 내역 생성
     *
     * @param reservationId 예약 ID
     * @param amount 결제 금액
     * @return 결제 정보를 담은 PaymentDTO 객체
     */
    public PaymentDTO processPayment(long customerId, long concertId, long reservationId, long amount) {
        reservationManager.tryPaymentWithLock(reservationId, amount);
        return reservationManager.PaymentCompleteWithLock(customerId, concertId, reservationId, amount);
    }

    /**
     * 주기적으로 만료된 예약을 확인하여 취소
     * <p>
     * 만료된 예약 목록을 가져온 후, 각 예약을 취소하고,
     * 해당 예약에 연결된 좌석도 취소합니다.
     * 마지막으로, 변경된 예약과 좌석 정보를 저장합니다.
     * </p>
     */
    public void cancelExpiredReservations() {
        List<Reservation> reservations = reservationService.cancelExpiredReservations();

        // 예약 취소
        for (Reservation reservation : reservations) {
            reservation.cancel();
            reservationService.save(reservation);
            Seat seat = seatService.findById(reservation.getSeatId());
            seat.cancel();
            seatService.save(seat);
        }
    }
}
