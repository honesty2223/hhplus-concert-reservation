package hhplus.concert.reservation.presentation.controller.reservation;

import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
import hhplus.concert.reservation.application.reservation.dto.ReservationDTO;
import hhplus.concert.reservation.application.reservation.usecase.ReservationUsecase;
import hhplus.concert.reservation.presentation.controller.reservation.request.PaymentRequest;
import hhplus.concert.reservation.presentation.controller.reservation.request.ReservationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "예약 Controller", description = "좌석 예약 요청 API, 결제 API")
public class ReservationController {

    private final ReservationUsecase reservationUsecase;

    public ReservationController(ReservationUsecase reservationUsecase) {
        this.reservationUsecase = reservationUsecase;
    }

    /**
     * 좌석 예약 요청 API
     *
     * @param reservationRequest 예약 요청 정보 (seatId, customerId)
     * @return 예약 정보를 포함한 응답
     */
    @Operation(
            summary = "좌석 예약 요청",
            security = {@SecurityRequirement(name = "headerAuth")}
    )
    @PostMapping("/reservation")
    public ResponseEntity<ReservationDTO> reserveSeat(@RequestBody ReservationRequest reservationRequest) {
        ReservationDTO reservationDTO = reservationUsecase.createReservation(reservationRequest.getSeatId(), reservationRequest.getCustomerId());
        return ResponseEntity.ok(reservationDTO);
    }

    /**
     * 결제 API
     *
     * @param paymentRequest 결제 요청 정보 (customerId, concertId, reservationId, amount)
     * @return 결제 정보를 포함한 응답
     */
    @Operation(
            summary = "결제",
            security = {@SecurityRequirement(name = "headerAuth")}
    )
    @PostMapping("/reservation/pay")
    public ResponseEntity<PaymentDTO> payForReservation(@RequestBody PaymentRequest paymentRequest) {
        PaymentDTO paymentDTO = reservationUsecase.processPayment(paymentRequest.getCustomerId(), paymentRequest.getConcertId(), paymentRequest.getReservationId(), paymentRequest.getAmount());
        return ResponseEntity.ok(paymentDTO);
    }

    /**
     * 결제 정보 전송 Mock API
     *
     */
    @PostMapping("/payment")
    public ResponseEntity<PaymentDTO> testApi(@RequestBody PaymentDTO paymentDTO) {
        return ResponseEntity.ok(paymentDTO);
    }
}
