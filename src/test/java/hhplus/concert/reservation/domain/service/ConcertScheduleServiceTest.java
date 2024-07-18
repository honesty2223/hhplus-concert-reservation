package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.concertSchedule.entity.ConcertSchedule;
import hhplus.concert.reservation.domain.concertSchedule.repository.ConcertScheduleRepository;
import hhplus.concert.reservation.domain.concertSchedule.service.ConcertScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConcertScheduleServiceTest {

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @InjectMocks
    private ConcertScheduleService concertScheduleService;

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
    @DisplayName("예약 가능 날짜 목록 조회 테스트")
    public void findByConcertIdTest() {
        // given
        long concertId = 1;
        List<ConcertSchedule> mockSchedules = Arrays.asList(
                new ConcertSchedule(1, concertId, 50, date1, createTime, updateTime),
                new ConcertSchedule(2, concertId, 50, date2, createTime, updateTime),
                new ConcertSchedule(3, concertId, 50, date3, createTime, updateTime)
        );

        // Mock 데이터 설정
        when(concertScheduleRepository.findByConcertId(concertId)).thenReturn(mockSchedules);

        // when
        List<ConcertSchedule> result = concertScheduleService.findByConcertId(concertId);

        // then
        assertEquals(mockSchedules.size(), result.size());
        assertEquals(mockSchedules, result);
        verify(concertScheduleRepository, times(1)).findByConcertId(concertId);
    }

    @Test
    @DisplayName("예약 가능 날짜가 존재하지 않을 때 예외 발생 테스트")
    public void findByConcertId_ScheduleNotFound() {
        // given
        long concertId = 500;
        when(concertScheduleRepository.findByConcertId(concertId)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(RuntimeException.class, () -> concertScheduleService.findByConcertId(concertId));
        verify(concertScheduleRepository, times(1)).findByConcertId(concertId);
    }

    @Test
    @DisplayName("콘서트 일정 저장 테스트")
    public void saveTest() {
        // given
        ConcertSchedule concertSchedule = new ConcertSchedule(1, 1, 50, date1, createTime, updateTime);

        // Mock 데이터 설정
        when(concertScheduleRepository.save(any(ConcertSchedule.class))).thenReturn(concertSchedule);

        // when
        ConcertSchedule savedConcertSchedule = concertScheduleService.save(concertSchedule);

        // then
        assertEquals(concertSchedule, savedConcertSchedule);
        verify(concertScheduleRepository, times(1)).save(any(ConcertSchedule.class));
    }
}