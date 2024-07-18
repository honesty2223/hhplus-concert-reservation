package hhplus.concert.reservation.application.reservation.usecase;

import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
import hhplus.concert.reservation.application.reservation.dto.ReservationDTO;
import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.service.CustomerService;
import hhplus.concert.reservation.domain.payment.entity.Payment;
import hhplus.concert.reservation.domain.payment.service.PaymentService;
import hhplus.concert.reservation.domain.reservation.entity.Reservation;
import hhplus.concert.reservation.domain.reservation.service.ReservationService;
import hhplus.concert.reservation.domain.seat.entity.Seat;
import hhplus.concert.reservation.domain.seat.service.SeatService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationUsecase {

    private final SeatService seatService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final CustomerService customerService;

    public ReservationUsecase(SeatService seatService, ReservationService reservationService, PaymentService paymentService, CustomerService customerService) {
        this.seatService = seatService;
        this.reservationService = reservationService;
        this.paymentService = paymentService;
        this.customerService = customerService;
    }

    /**
     * 좌석 예약 요청
     *
     * @return 예약 정보를 담은 ReservationDTO 객체 리스트
     */
    public ReservationDTO createReservation(long seatId, long customerId) {
        Seat seat = seatService.findById(seatId);
        seat.reserveSeat(customerId);
        seatService.save(seat);

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
    public PaymentDTO processPayment(long reservationId, long amount) {
        // 1. 예약 조회
        Reservation reservation = reservationService.findById(reservationId);

        // 2. 고객 조회
        Customer customer = customerService.findById(reservation.getCustomerId());

        // 3. 포인트 차감
        customer.deductPoint(amount);
        customerService.save(customer);

        // 4. 결제 처리
        Payment payment = new Payment(customer.getCustomerId(), reservationId, amount);
        Payment savedPayment = paymentService.save(payment);

        if (savedPayment != null) {
            // 5. 예약 상태 업데이트 및 좌석 소유권 배정
            reservation.completed();
            reservationService.save(reservation);

            Seat seat = seatService.findById(reservation.getSeatId());
            seat.completed();
            seatService.save(seat);

            return new PaymentDTO(savedPayment.getPaymentId(), savedPayment.getCustomerId(), savedPayment.getReservationId(), savedPayment.getAmount(), savedPayment.getPaymentTime(), savedPayment.getCreatedAt(), savedPayment.getUpdatedAt());
        } else {
            throw new CoreException(ErrorCode.PAYMENT_FAILED);
        }
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
