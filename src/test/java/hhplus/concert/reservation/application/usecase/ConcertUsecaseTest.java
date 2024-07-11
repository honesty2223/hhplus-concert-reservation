package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.dto.ConcertDTO;
import hhplus.concert.reservation.application.dto.ConcertScheduleDTO;
import hhplus.concert.reservation.application.dto.SeatDTO;
import hhplus.concert.reservation.domain.entity.Concert;
import hhplus.concert.reservation.domain.entity.ConcertSchedule;
import hhplus.concert.reservation.domain.entity.Seat;
import hhplus.concert.reservation.domain.entity.Token;
import hhplus.concert.reservation.domain.service.ConcertScheduleService;
import hhplus.concert.reservation.domain.service.ConcertService;
import hhplus.concert.reservation.domain.service.SeatService;
import hhplus.concert.reservation.domain.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ConcertUsecaseTest {

    @Autowired
    private ConcertService concertService;

    @Autowired
    private ConcertScheduleService concertScheduleService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ConcertUsecase concertUsecase;

    private LocalDate date1;
    private LocalDate date2;
    private LocalDate date3;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @BeforeEach
    public void setUp() {
        date1 = LocalDate.of(2024, 7, 27);
        date2 = LocalDate.of(2024, 7, 28);
        date3 = LocalDate.of(2024, 8, 3);
        createTime = LocalDateTime.now().minusHours(3);
        updateTime = LocalDateTime.now().minusHours(1);
    }

    @Test
    @DisplayName("모든 콘서트 조회 테스트")
    public void getAllConcertsTest() {
        // given
        List<Concert> concerts = Arrays.asList(
                new Concert(1, "이무진 콘서트", createTime, updateTime),
                new Concert(2, "소수빈 콘서트", createTime, updateTime),
                new Concert(3, "뉴진스 콘서트", createTime, updateTime)
        );

        // Save each concert
        for (Concert concert : concerts) {
            concertService.save(concert);
        }

        // when
        List<ConcertDTO> result = concertUsecase.getAllConcerts();

        // then
        assertNotNull(result); // 결과가 null이 아닌지 확인
        assertEquals(concerts.size(), result.size()); // 저장된 콘서트 수와 조회된 콘서트 수가 일치하는지 확인

        // 각 콘서트 객체와 DTO의 필드 값이 일치하는지 확인
        for (int i = 0; i < concerts.size(); i++) {
            Concert expectedConcert = concerts.get(i);
            ConcertDTO actualConcertDTO = result.get(i);

            assertEquals(expectedConcert.getConcertId(), actualConcertDTO.getConcertId());
            assertEquals(expectedConcert.getConcertName(), actualConcertDTO.getConcertName());
        }
    }

    @Test
    @DisplayName("예약 가능 날짜 목록 테스트")
    public void getAvailableDatesTest() {
        // given
        Token token = new Token(1, 1, 1, 1, "ACTIVE", createTime, updateTime);
        tokenService.save(token);

        long concertId = 1L;
        List<ConcertSchedule> schedules = Arrays.asList(
                new ConcertSchedule(1, concertId, 50, date1, createTime, updateTime),
                new ConcertSchedule(2, concertId, 50, date2, createTime, updateTime),
                new ConcertSchedule(3, concertId, 50, date3, createTime, updateTime)
        );

        // Save each schedule
        for (ConcertSchedule schedule : schedules) {
            concertScheduleService.save(schedule);
        }

        // when
        List<ConcertScheduleDTO> result = concertUsecase.getAvailableDates(token.getTokenId(), concertId);

        // then
        assertNotNull(result); // 결과가 null이 아닌지 확인
        assertEquals(schedules.size(), result.size()); // 저장된 일정 수와 조회된 일정 수가 일치하는지 확인

        // 각 콘서트 일정 객체와 DTO의 필드 값이 일치하는지 확인
        for (int i = 0; i < schedules.size(); i++) {
            ConcertSchedule expectedSchedule = schedules.get(i);
            ConcertScheduleDTO actualScheduleDTO = result.get(i);

            assertEquals(expectedSchedule.getConcertScheduleId(), actualScheduleDTO.getConcertScheduleId());
            assertEquals(expectedSchedule.getSeatCount(), actualScheduleDTO.getSeatCount());
            assertEquals(expectedSchedule.getConcertDate(), actualScheduleDTO.getConcertDate());
        }
    }

    @Test
    @DisplayName("예약 가능 좌석 목록 테스트")
    public void getAvailableSeatsTest() {
        // given
        Token token = new Token(1, 1, 1, 1, "ACTIVE", createTime, updateTime);
        tokenService.save(token);

        long concertScheduleId = 1L;
        List<Seat> seats = Arrays.asList(
                new Seat(1, concertScheduleId, 1, 7000, true, 0, null, createTime, updateTime),
                new Seat(2, concertScheduleId, 3, 60000, false, 0, null, createTime, updateTime),
                new Seat(3, concertScheduleId, 15, 50000, false, 0, null, createTime, updateTime)
        );

        // Save each seat
        for (Seat seat : seats) {
            seatService.save(seat);
        }

        // when
        List<SeatDTO> result = concertUsecase.getAvailableSeats(token.getTokenId(), concertScheduleId);

        // then
        assertNotNull(result); // 결과가 null이 아닌지 확인
        assertEquals(seats.size(), result.size()); // 저장된 좌석 수와 조회된 좌석 수가 일치하는지 확인

        // 각 좌석 객체와 DTO의 필드 값이 일치하는지 확인
        for (int i = 0; i < seats.size(); i++) {
            Seat expectedSeat = seats.get(i);
            SeatDTO actualSeatDTO = result.get(i);

            assertEquals(expectedSeat.getSeatId(), actualSeatDTO.getSeatId());
            assertEquals(expectedSeat.getConcertScheduleId(), actualSeatDTO.getConcertScheduleId());
            assertEquals(expectedSeat.getSeatNumber(), actualSeatDTO.getSeatNumber());
            assertEquals(expectedSeat.getPrice(), actualSeatDTO.getPrice());
            assertEquals(expectedSeat.isFinallyReserved(), actualSeatDTO.isFinallyReserved());
            assertEquals(expectedSeat.getTempAssigneeId(), actualSeatDTO.getTempAssigneeId());
        }
    }
}