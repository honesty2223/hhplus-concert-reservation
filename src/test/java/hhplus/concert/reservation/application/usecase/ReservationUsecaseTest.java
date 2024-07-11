package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.dto.PaymentDTO;
import hhplus.concert.reservation.application.dto.ReservationDTO;
import hhplus.concert.reservation.domain.entity.*;
import hhplus.concert.reservation.domain.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReservationUsecaseTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ReservationUsecase reservationUsecase;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime expiredTime;
    private LocalDateTime validUntilTime;

    @BeforeEach
    public void setUp() {
        createTime = LocalDateTime.now().minusHours(3);
        updateTime = LocalDateTime.now().minusHours(1);
        expiredTime = LocalDateTime.now().minusMinutes(5);      // 현재 시간에서 5분 전, 만료된 시간
        validUntilTime = LocalDateTime.now().plusMinutes(3);    // 현재 시간에서 3분 후, 유효한 시간
    }

    @Test
    @DisplayName("좌석 예약 테스트")
    public void createReservationTest() {
        // given
        Token token = new Token(1, 1, 1, 1, "ACTIVE", createTime, updateTime);
        tokenService.save(token);

        Seat seat = new Seat(1, 1, 1, 7000, false, 0, expiredTime, createTime, updateTime);
        seatService.save(seat);

        // when
        ReservationDTO result = reservationUsecase.createReservation(token.getTokenId(), seat.getSeatId(), 1);

        // then
        assertNotNull(result);
        assertEquals(seat.getSeatId(), result.getSeatId());
        assertEquals(1, result.getCustomerId());
        assertNotNull(result.getReservationId()); // 자동 생성된 reservationId가 null이 아닌지 확인
        assertEquals(seat.getConcertScheduleId(), result.getConcertScheduleId());
        assertNotNull(result.getReservationTime()); // reservationTime이 null이 아닌지 확인
        assertEquals("PENDING", result.getStatus()); // 예약 상태가 "PENDING"인지 확인
    }

    @Test
    @DisplayName("좌석이 임시배정된 경우 다른 사람이 예약 요청 시 예외 발생")
    public void reserveSeatWhenExpired() {
        // given
        Token token = new Token(1, 1, 1, 1, "ACTIVE", createTime, updateTime);
        tokenService.save(token);

        Seat seat = new Seat(1, 1, 1, 7000, false, 1, validUntilTime, createTime, updateTime);
        seatService.save(seat);

        // when & then
        assertThrows(RuntimeException.class, () -> reservationUsecase.createReservation(token.getTokenId(), seat.getSeatId(), 1));
    }

    @Test
    @DisplayName("주기적으로 만료된 예약을 취소하는 메소드 테스트")
    public void cancelExpiredReservationsPeriodically() {
        // given
        List<Reservation> reservations = Arrays.asList(
                new Reservation(1, 1, 1, 1, expiredTime, "PENDING", createTime, updateTime),
                new Reservation(2, 2, 2, 1, expiredTime, "PENDING", createTime, updateTime),
                new Reservation(3, 3, 3, 1, expiredTime, "PENDING", createTime, updateTime)
        );

        // Save each seat
        for (Reservation reservation : reservations) {
            reservationService.save(reservation);
        }

        // when
        reservationUsecase.cancelExpiredReservationsPeriodically();

        // then
        Reservation result1 = reservationService.findById(1);
        Reservation result2 = reservationService.findById(2);
        Reservation result3 = reservationService.findById(3);
        assertEquals("CANCELLED", result1.getStatus());
        assertEquals("CANCELLED", result2.getStatus());
        assertEquals("CANCELLED", result3.getStatus());
    }

    @Test
    @DisplayName("결제 처리 및 결제 내역 생성 테스트")
    public void processPaymentTest() {
        // given
        Token token = new Token(1, 1, 1, 1, "ACTIVE", createTime, updateTime);
        tokenService.save(token);

        Customer customer = new Customer(1, "홍길동", 10000, createTime, updateTime); // 포인트가 충분한 고객
        customerService.save(customer);

        Seat seat = new Seat(1, 1, 1, 7000, false, customer.getCustomerId(), validUntilTime, createTime, updateTime);
        seatService.save(seat);

        Reservation reservation = new Reservation(1, customer.getCustomerId(), seat.getSeatId(), seat.getConcertScheduleId(), LocalDateTime.now(), "PENDING", createTime, updateTime);
        reservationService.save(reservation);

        // when
        PaymentDTO paymentDTO = reservationUsecase.processPayment(token.getTokenId(), reservation.getReservationId(), 7000);

        // then
        Payment payment = paymentService.findById(paymentDTO.getPaymentId());
        assertNotNull(payment);
        assertEquals(reservation.getReservationId(), payment.getReservationId());
        assertEquals(7000, payment.getAmount());
        assertNotNull(payment.getPaymentTime()); // 결제 시간이 null이 아닌지 확인

        // Check reservation status
        Reservation updatedReservation = reservationService.findById(reservation.getReservationId());
        assertEquals("COMPLETED", updatedReservation.getStatus());

        // Check seat status
        Seat updatedSeat = seatService.findById(seat.getSeatId());
        assertTrue(updatedSeat.isFinallyReserved());

        // Check customer points
        Customer updatedCustomer = customerService.findById(customer.getCustomerId());
        assertEquals(3000, updatedCustomer.getPoint()); // 포인트가 7000 차감되어 3000 남아야 함
    }
}