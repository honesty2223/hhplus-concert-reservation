package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.dto.PaymentDTO;
import hhplus.concert.reservation.application.dto.ReservationDTO;
import hhplus.concert.reservation.domain.entity.Customer;
import hhplus.concert.reservation.domain.entity.Payment;
import hhplus.concert.reservation.domain.entity.Reservation;
import hhplus.concert.reservation.domain.entity.Seat;
import hhplus.concert.reservation.domain.service.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationUsecase {

    private final SeatService seatService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final CustomerService customerService;
    private final TokenService tokenService;

    public ReservationUsecase(SeatService seatService, ReservationService reservationService, PaymentService paymentService, CustomerService customerService, TokenService tokenService) {
        this.seatService = seatService;
        this.reservationService = reservationService;
        this.paymentService = paymentService;
        this.customerService = customerService;
        this.tokenService = tokenService;
    }

    /**
     * 좌석 예약 요청
     *
     * @return 예약 정보를 담은 ReservationDTO 객체 리스트
     */
    public ReservationDTO createReservation(long tokenId, long seatId, long customerId) {
        // 토큰의 활성화 여부를 확인합니다. 비활성화된 경우 예외가 발생합니다.
        tokenService.isActiveToken(tokenId);

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
    public PaymentDTO processPayment(long tokenId, long reservationId, long amount) {
        // 토큰의 활성화 여부를 확인합니다. 비활성화된 경우 예외가 발생합니다.
        tokenService.isActiveToken(tokenId);

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
            throw new RuntimeException("결제 처리에 실패했습니다.");
        }
    }

    // 주기적으로 만료된 예약을 취소합니다
    @Scheduled(fixedRate = 60000) // 매분 실행
    public void cancelExpiredReservationsPeriodically() {
        reservationService.cancelExpiredReservations();
    }
}
