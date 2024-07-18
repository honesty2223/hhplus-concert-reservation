package hhplus.concert.reservation.presentation.reservation;

import hhplus.concert.reservation.application.reservation.dto.PaymentDTO;
import hhplus.concert.reservation.application.reservation.dto.ReservationDTO;
import hhplus.concert.reservation.application.reservation.usecase.ReservationUsecase;
import hhplus.concert.reservation.presentation.reservation.request.PaymentRequest;
import hhplus.concert.reservation.presentation.reservation.request.ReservationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @PostMapping("/reservation")
    public ResponseEntity<ReservationDTO> reserveSeat(@RequestHeader("Token-ID") long tokenId, @RequestBody ReservationRequest reservationRequest) {
        ReservationDTO reservationDTO = reservationUsecase.createReservation(tokenId, reservationRequest.getSeatId(), reservationRequest.getCustomerId());
        return ResponseEntity.ok(reservationDTO);
    }

    /**
     * 결제 API
     *
     * @param paymentRequest 결제 요청 정보 (reservationId, amount)
     * @return 결제 정보를 포함한 응답
     */
    @PostMapping("/reservation/pay")
    public ResponseEntity<PaymentDTO> payForReservation(@RequestHeader("Token-ID") long tokenId, @RequestBody PaymentRequest paymentRequest) {
        PaymentDTO paymentDTO = reservationUsecase.processPayment(tokenId, paymentRequest.getReservationId(), paymentRequest.getAmount());
        return ResponseEntity.ok(paymentDTO);
    }
}
