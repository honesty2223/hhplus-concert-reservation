package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.concert.entity.Concert;
import hhplus.concert.reservation.domain.concert.repository.ConcertRepository;
import hhplus.concert.reservation.domain.concert.service.ConcertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConcertServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private ConcertService concertService;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @BeforeEach
    public void setUp() {
        createTime = LocalDateTime.now().minusHours(3);
        updateTime = LocalDateTime.now().minusHours(1);
    }

    @Test
    @DisplayName("콘서트 전체 조회 테스트")
    public void findAllTest() {
        // given
        List<Concert> mockConcerts = Arrays.asList(
                new Concert(1, "이무진 콘서트", createTime, updateTime),
                new Concert(2, "소수빈 콘서트", createTime, updateTime),
                new Concert(3, "뉴진스 콘서트", createTime, updateTime)
        );

        // Mock 데이터 설정
        when(concertRepository.findAll()).thenReturn(mockConcerts);

        // when
        List<Concert> result = concertService.findAll();

        // then
        assertEquals(mockConcerts.size(), result.size());
        assertEquals(mockConcerts, result);
        verify(concertRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("콘서트 조회 테스트")
    public void findByIdTest() {
        // given
        long concertId = 1;
        Concert mockConcert = new Concert(1, "이무진 콘서트", createTime, updateTime);

        // Mock 데이터 설정
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(mockConcert));

        // when
        Concert result = concertService.findById(concertId);

        // then
        assertEquals(mockConcert, result);
        verify(concertRepository, times(1)).findById(concertId);
    }

    @Test
    @DisplayName("콘서트가 존재하지 않을 때 예외 발생 테스트")
    public void findById_ConcertNotFound() {
        // given
        long concertId = 500;
        when(concertRepository.findById(concertId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> concertService.findById(concertId));
        verify(concertRepository, times(1)).findById(concertId);
    }

    @Test
    @DisplayName("콘서트 저장 테스트")
    public void saveTest() {
        // given
        Concert concert = new Concert(1, "이무진 콘서트", createTime, updateTime);

        // Mock 데이터 설정
        when(concertRepository.save(any(Concert.class))).thenReturn(concert);

        // when
        Concert savedConcert = concertService.save(concert);

        // then
        assertEquals(concert, savedConcert);
        verify(concertRepository, times(1)).save(any(Concert.class));
    }
}
