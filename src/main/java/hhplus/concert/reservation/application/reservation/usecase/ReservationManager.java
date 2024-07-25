package hhplus.concert.reservation.application.reservation.usecase;

import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
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
import hhplus.concert.reservation.domain.token.entity.Token;
import hhplus.concert.reservation.domain.token.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ReservationManager {

    private final SeatService seatService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final CustomerService customerService;
    private final TokenService tokenService;

    public ReservationManager(SeatService seatService, ReservationService reservationService, PaymentService paymentService, CustomerService customerService, TokenService tokenService) {
        this.seatService = seatService;
        this.reservationService = reservationService;
        this.paymentService = paymentService;
        this.customerService = customerService;
        this.tokenService = tokenService;
    }

    @Transactional
    public void tryPaymentWithLock(long reservationId, long amount) {
        log.info("{}>> [Pessimistic Lock] tryPaymentWithLock 시작, 예약ID: {}", Thread.currentThread().getName(), reservationId);
        long startTime = System.currentTimeMillis(); // 시작 시간 기록

        try {
            // 1. 예약 조회
            Reservation reservation = reservationService.findByIdWithLock(reservationId);
            if ("COMPLETED".equals(reservation.getStatus())) {
                throw new CoreException(ErrorCode.RESERVATION_ALREADY_PROCESSED);
            }

            // 2. 고객 조회
            Customer customer = customerService.findByIdWithLock(reservation.getCustomerId());

            // 3. 포인트 차감
            customer.deductPoint(amount);
            customerService.save(customer);

            // 4. 예약 상태 업데이트 및 좌석 소유권 배정
            reservation.completed();
            reservationService.save(reservation);

            Seat seat = seatService.findByIdWithLock(reservation.getSeatId());
            seat.completed();
            seatService.save(seat);

            log.info("{}>> [Pessimistic Lock] tryPaymentWithLock 완료, 예약ID: {}", Thread.currentThread().getName(), reservationId);

        } catch (Exception e) {
            log.error("{}>> [Pessimistic Lock] tryPaymentWithLock 예외 발생: 예약ID: {}, 오류 메시지: {}",
                    Thread.currentThread().getName(), reservationId, e.getMessage());
            throw e;
        } finally {
            long endTime = System.currentTimeMillis(); // 종료 시간 기록
            long duration = endTime - startTime;
            log.info("{}>> [Pessimistic Lock] tryPaymentWithLock 종료, 소요 시간: {} ms", Thread.currentThread().getName(), duration);
        }
    }

    @Transactional
    public PaymentDTO PaymentCompleteWithLock(long customerId, long concertId, long reservationId, long amount) {
        long startTime = System.currentTimeMillis(); // 시작 시간 기록
        log.info("{}>> [Pessimistic Lock] PaymentCompleteWithLock 시작, 고객ID: {}, 예약ID: {}",
                Thread.currentThread().getName(), customerId, reservationId);

        Payment savedPayment = null; // try 블록 외부에서 선언

        try {
            // 1. 결제 내역 생성
            Payment payment = new Payment(customerId, reservationId, amount);
            savedPayment = paymentService.save(payment); // 변수 할당

            // 2. 토큰 만료 처리
            Token token = tokenService.findByCustomerIdAndConcertIdWithLock(customerId, concertId);
            token.markAsExpired();
            tokenService.save(token);

            log.info("{}>> [Pessimistic Lock] PaymentCompleteWithLock 완료, 고객ID: {}, 예약ID: {}",
                    Thread.currentThread().getName(), customerId, reservationId);

        } catch (Exception e) {
            log.error("{}>> [Pessimistic Lock] PaymentCompleteWithLock 예외 발생: 고객ID: {}, 예약ID: {}, 오류 메시지: {}",
                    Thread.currentThread().getName(), customerId, reservationId, e.getMessage());
            throw e;
        } finally {
            long endTime = System.currentTimeMillis(); // 종료 시간 기록
            long duration = endTime - startTime;
            log.info("{}>> [Pessimistic Lock] PaymentCompleteWithLock 종료, 소요 시간: {} ms", Thread.currentThread().getName(), duration);
        }

        return new PaymentDTO(
                savedPayment.getPaymentId(),
                savedPayment.getCustomerId(),
                savedPayment.getReservationId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentTime(),
                savedPayment.getCreatedAt(),
                savedPayment.getUpdatedAt()
        );
    }
}
