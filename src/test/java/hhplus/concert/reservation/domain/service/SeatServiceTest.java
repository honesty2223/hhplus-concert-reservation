package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.entity.Seat;
import hhplus.concert.reservation.domain.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatService seatService;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @BeforeEach
    public void setUp() {
        createTime = LocalDateTime.now().minusHours(3);
        updateTime = LocalDateTime.now().minusHours(1);
    }

    @Test
    @DisplayName("예약 가능 좌석 목록 조회 테스트")
    public void findAvailableSeatsTest() {
        // given
        long concertScheduleId = 1;
        List<Seat> mockSeats = Arrays.asList(
                new Seat(1, concertScheduleId, 1, 7000, false, 0, null, createTime, updateTime),
                new Seat(2, concertScheduleId, 3, 60000, false, 0, null, createTime, updateTime),
                new Seat(3, concertScheduleId, 15, 50000, false, 0, null, createTime, updateTime)
        );

        // Mock 데이터 설정
        when(seatRepository.findAvailableSeats(concertScheduleId)).thenReturn(mockSeats);

        // when
        List<Seat> result = seatService.findAvailableSeats(concertScheduleId);

        // then
        assertEquals(mockSeats.size(), result.size());
        assertEquals(mockSeats, result);
        verify(seatRepository, times(1)).findAvailableSeats(concertScheduleId);
    }

    @Test
    @DisplayName("예약 가능 좌석가 존재하지 않을 때 예외 발생 테스트")
    public void findAvailableSeats_SeatNotFound() {
        // given
        long concertScheduleId = 300;
        when(seatRepository.findAvailableSeats(concertScheduleId)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(RuntimeException.class, () -> seatService.findAvailableSeats(concertScheduleId));
        verify(seatRepository, times(1)).findAvailableSeats(concertScheduleId);
    }

    @Test
    @DisplayName("좌석 저장 테스트")
    public void saveTest() {
        // given
        Seat seat = new Seat(1, 2, 1, 7000, false, 0, null, createTime, updateTime);

        // Mock 데이터 설정
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);

        // when
        Seat savedSeat = seatService.save(seat);

        // then
        assertEquals(seat, savedSeat);
        verify(seatRepository, times(1)).save(any(Seat.class));
    }
}