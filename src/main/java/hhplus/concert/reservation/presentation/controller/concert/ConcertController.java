package hhplus.concert.reservation.presentation.controller.concert;

import hhplus.concert.reservation.application.concert.dto.ConcertDTO;
import hhplus.concert.reservation.application.concert.dto.ConcertScheduleDTO;
import hhplus.concert.reservation.application.concert.dto.SeatDTO;
import hhplus.concert.reservation.application.concert.usecase.ConcertUsecase;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "콘서트 Controller", description = "예약 가능 날짜 조회 API, 예약 가능 좌석 조회 API")
public class ConcertController {

    private final ConcertUsecase concertUsecase;

    public ConcertController(ConcertUsecase concertUsecase) {
        this.concertUsecase = concertUsecase;
    }

    /**
     * 콘서트 조회 API
     *
     * @return 콘서트 목록을 포함한 응답
     */
    @GetMapping("/concerts")
    public ResponseEntity<List<ConcertDTO>> getAllConcerts() {
        List<ConcertDTO> concertDTO = concertUsecase.getAllConcerts();
        return ResponseEntity.ok(concertDTO);
    }

    /**
     * 예약 가능한 날짜 조회 API
     *
     * @return 예약 가능한 날짜 목록을 포함한 응답
     */
    @GetMapping("/concerts/{concertId}")
    public ResponseEntity<List<ConcertScheduleDTO>> getConcertSchedule(@RequestHeader("Token-ID") long tokenId, @PathVariable long concertId) {
        List<ConcertScheduleDTO> concertScheduleDTO = concertUsecase.getAvailableDates(tokenId, concertId);
        return ResponseEntity.ok(concertScheduleDTO);
    }

    /**
     * 예약 가능한 좌석 조회 API
     *
     * @return 예약 가능한 좌석 목록을 포함한 응답
     */
    @GetMapping("/concerts/{concertScheduleId}/seat")
    public ResponseEntity<List<SeatDTO>> getConcertSeat(@RequestHeader("Token-ID") long tokenId, @PathVariable int concertScheduleId) {
        List<SeatDTO> seatDTO = concertUsecase.getAvailableSeats(tokenId, concertScheduleId);
        return ResponseEntity.ok(seatDTO);
    }
}
