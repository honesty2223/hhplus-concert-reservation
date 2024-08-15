package hhplus.concert.reservation.presentation.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
import hhplus.concert.reservation.application.reservation.usecase.ReservationUsecase;
import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.service.CustomerService;
import hhplus.concert.reservation.domain.outbox.entity.Outbox;
import hhplus.concert.reservation.domain.outbox.entity.OutboxStatus;
import hhplus.concert.reservation.domain.outbox.repository.OutboxRepository;
import hhplus.concert.reservation.domain.outbox.service.OutboxService;
import hhplus.concert.reservation.domain.payment.event.PaymentCompletedEvent;
import hhplus.concert.reservation.domain.reservation.entity.Reservation;
import hhplus.concert.reservation.domain.reservation.service.ReservationService;
import hhplus.concert.reservation.domain.seat.entity.Seat;
import hhplus.concert.reservation.domain.seat.service.SeatService;
import hhplus.concert.reservation.domain.token.entity.Token;
import hhplus.concert.reservation.domain.token.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ReservationUsecase reservationUsecase;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime validUntilTime;

    @BeforeEach
    public void setUp() {
        createTime = LocalDateTime.now().minusHours(3);
        updateTime = LocalDateTime.now().minusHours(1);
        validUntilTime = LocalDateTime.now().plusMinutes(3);    // 현재 시간에서 3분 후, 유효한 시간
    }

    @Test
    @DisplayName("카프카 메시지 발행 및 소비 테스트 & 아웃박스 로직 테스트")
    public void kafkaTest() throws Exception {
        // given
        Token token = new Token(1, 1, 1, 1, "ACTIVE", createTime, updateTime);
        tokenService.save(token);

        Customer customer = new Customer(1, "홍길동", 10000, createTime, updateTime);
        customerService.save(customer);

        Seat seat = new Seat(1, 1, 1, 7000, false, customer.getCustomerId(), validUntilTime, createTime, updateTime);
        seatService.save(seat);

        Reservation reservation = new Reservation(1, customer.getCustomerId(), seat.getSeatId(), seat.getConcertScheduleId(), LocalDateTime.now(), "PENDING", createTime, updateTime);
        reservationService.save(reservation);

        // when
        PaymentDTO paymentDTO = reservationUsecase.processPayment(customer.getCustomerId(), token.getConcertId(), reservation.getReservationId(), 7000);

        // 컨슈머가 메시지를 처리할 시간을 줌
        Thread.sleep(5000);

        // Check Outbox status (컨슈머 처리로 인해 status 값이 INIT 에서 PUBLISHED 로 변경됨을 확인)
        Outbox updatedOutbox = outboxRepository.findByPaymentId(paymentDTO.getPaymentId());
        assertEquals(OutboxStatus.PUBLISHED, updatedOutbox.getStatus());
    }
}
