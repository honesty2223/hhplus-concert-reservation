package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.token.entity.Token;
import hhplus.concert.reservation.domain.token.repository.TokenRepository;
import hhplus.concert.reservation.domain.token.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("콘서트 대기열 참가 테스트")
    public void generateNewTokenTest() {
        // given
        long customerId = 1;
        long concertId = 1;
        Optional<Long> expectedWaitNumber = Optional.of(1L);
        long nextWaitNumber = expectedWaitNumber.orElse(0L) + 1;
        Token mockToken = new Token(1, concertId, customerId, nextWaitNumber, "PENDING", now, null);

        // Mock 데이터 설정
        when(tokenRepository.findMaxPositionByConcertId(concertId)).thenReturn(expectedWaitNumber);
        when(tokenRepository.save(any(Token.class))).thenReturn(mockToken);

        // when
        Token result = tokenService.generateNewToken(customerId, concertId);

        // then
        assertEquals(mockToken, result);
        verify(tokenRepository, times(1)).findMaxPositionByConcertId(concertId);
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    @DisplayName("이미 해당 콘서트 대기열에 참가한 고객일 경우 예외 발생 테스트")
    public void generateNewTokenErrorTest() {
        // given
        long customerId = 1;
        long concertId = 1;
        Token mockToken = new Token(30, concertId, customerId, 50, "PENDING", now, null);
        when(tokenRepository.findByCustomerId(customerId, concertId)).thenReturn(mockToken);

        // when & then
        assertThrows(RuntimeException.class, () -> tokenService.generateNewToken(customerId, concertId));
        verify(tokenRepository, times(1)).findByCustomerId(customerId, concertId);
        verify(tokenRepository, times(0)).findMaxPositionByConcertId(concertId);
        verify(tokenRepository, times(0)).save(any(Token.class));
    }

    @Test
    @DisplayName("콘서트 대기열 조회 테스트")
    public void checkTokenTest() {
        // given
        long customerId = 2;
        long concertId = 1;
        Token mockToken = new Token(2, concertId, customerId, 2, "PENDING", now, null);
        Token lastActiveToken = new Token(1, concertId, 1, 1, "ACTIVE", now.minusMinutes(10), now);

        // Mock 데이터 설정
        when(tokenRepository.findByCustomerId(customerId, concertId)).thenReturn(mockToken);
        when(tokenRepository.findActiveTokensByConcertId(concertId)).thenReturn(List.of(lastActiveToken));

        // when
        Token result = tokenService.checkToken(customerId, concertId);

        // then
        assertEquals(mockToken.getTokenId(), result.getTokenId());
        assertEquals(mockToken.getCustomerId(), result.getCustomerId());
        assertEquals(mockToken.getConcertId(), result.getConcertId());
        assertEquals(1, result.getWaitNumber());
        assertEquals(mockToken.getStatus(), result.getStatus());
        assertEquals(mockToken.getCreatedAt(), result.getCreatedAt());
        assertNull(result.getUpdatedAt());
        verify(tokenRepository, times(1)).findByCustomerId(customerId, concertId);
        verify(tokenRepository, times(1)).findActiveTokensByConcertId(concertId);
    }

    @Test
    @DisplayName("대기열에 참가되어 있지 않은 고객일 경우 예외 발생 테스트")
    public void checkTokenErrorTest() {
        // given
        long customerId = 1;
        long concertId = 1;
        when(tokenRepository.findByCustomerId(customerId, concertId)).thenReturn(null);

        // when & then
        assertThrows(RuntimeException.class, () -> tokenService.checkToken(customerId, concertId));
        verify(tokenRepository, times(1)).findByCustomerId(customerId, concertId);
        verify(tokenRepository, times(0)).findActiveTokensByConcertId(concertId);
    }

    @Test
    @DisplayName("토큰 활성화 여부 조회 테스트")
    public void isActiveTokenTest() {
        // given
        long tokenId = 1;
        Token mockToken = new Token(1, 1, 1, 2, "ACTIVE", now.minusMinutes(10), now);

        // Mock 데이터 설정
        when(tokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(mockToken));

        // when
        boolean result = tokenService.isActiveToken(tokenId);

        // then
        assertTrue(result);
        verify(tokenRepository, times(1)).findByTokenId(tokenId);
    }

    @Test
    @DisplayName("토큰이 존재하지 않을 때 예외 발생 테스트")
    public void isActiveTokenErrorTest() {
        // given
        long tokenId = 1;
        when(tokenRepository.findByTokenId(tokenId)).thenReturn(null);

        // when & then
        assertThrows(RuntimeException.class, () -> tokenService.isActiveToken(tokenId));
        verify(tokenRepository, times(1)).findByTokenId(tokenId);
    }

    @Test
    @DisplayName("토큰 활성화(대기열 통과) 테스트")
    public void activeTokenTest() {
        // given
        long concertId = 1;
        int size = 3;
        List<Token> mockTokens = Arrays.asList(
                new Token(1, concertId, 1, 1, "PENDING", now.minusMinutes(3), null),
                new Token(2, concertId, 2, 2, "PENDING", now.minusMinutes(2), null),
                new Token(3, concertId, 3, 3, "PENDING", now.minusMinutes(1), null)
        );
        List<Token> ativeTokens = new ArrayList<>();

        // Mock 데이터 설정
        when(tokenRepository.findPendingTokensByConcertId(concertId)).thenReturn(mockTokens);
        when(tokenRepository.findActiveTokensByConcertId(concertId)).thenReturn(ativeTokens);

        // when
        List<Token> result = tokenService.activeToken(concertId, size);

        // then
        assertEquals(size, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
        assertEquals("ACTIVE", result.get(1).getStatus());
        assertEquals("ACTIVE", result.get(2).getStatus());
        System.out.println(result.get(0).getUpdatedAt());
        System.out.println(result.get(1).getUpdatedAt());
        System.out.println(result.get(2).getUpdatedAt());
        verify(tokenRepository, times(1)).findPendingTokensByConcertId(concertId);
        verify(tokenRepository, times(1)).findActiveTokensByConcertId(concertId);
        verify(tokenRepository, times(3)).save(any(Token.class));
    }

    @Test
    @DisplayName("토큰 만료 테스트")
    public void expireTokenTest() {
        // given
        long concertId = 1;
        List<Token> mockTokens = Arrays.asList(
                new Token(1, concertId, 1, 1, "ACTIVE", now.minusMinutes(60), now.minusMinutes(50)),
                new Token(2, concertId, 2, 2, "ACTIVE", now.minusMinutes(50), now.minusMinutes(40)),
                new Token(3, concertId, 3, 3, "ACTIVE", now.minusMinutes(40), now.minusMinutes(30))
        );

        // Mock 데이터 설정
        when(tokenRepository.findActiveTokensByConcertId(concertId)).thenReturn(mockTokens);

        // when
        List<Token> result = tokenService.expireToken(concertId);

        // then
        assertEquals(mockTokens.size(), result.size());
        assertEquals("EXPIRED", result.get(0).getStatus());
        assertEquals("EXPIRED", result.get(1).getStatus());
        assertEquals("EXPIRED", result.get(2).getStatus());
        verify(tokenRepository, times(1)).findActiveTokensByConcertId(concertId);
        verify(tokenRepository, times(3)).save(any(Token.class));
    }

    @Test
    @DisplayName("고객 ID로 토큰 조회 테스트")
    public void findByCustomerIdTest() {
        // given
        long customerId = 1;
        long concertId = 1;
        Token mockToken = new Token(1, concertId, customerId, 1, "PENDING", now, null);

        // Mock 데이터 설정
        when(tokenRepository.findByCustomerId(customerId, concertId)).thenReturn(mockToken);

        // when
        Token result = tokenService.findByCustomerId(customerId, concertId);

        // then
        assertEquals(mockToken, result);
        verify(tokenRepository, times(1)).findByCustomerId(customerId, concertId);
    }

    @Test
    @DisplayName("콘서트 별 활성화 토큰 조회 테스트")
    public void findActiveTokensByConcertIdTest() {
        // given
        long concertId = 1;
        List<Token> mockTokens = Arrays.asList(
                new Token(1, concertId, 1, 1, "ACTIVE", now.minusMinutes(12), now),
                new Token(2, concertId, 2, 2, "ACTIVE", now.minusMinutes(11), now),
                new Token(3, concertId, 3, 3, "ACTIVE", now.minusMinutes(10), now)
        );

        // Mock 데이터 설정
        when(tokenRepository.findActiveTokensByConcertId(concertId)).thenReturn(mockTokens);

        // when
        List<Token> result = tokenService.findActiveTokensByConcertId(concertId);

        // then
        assertEquals(mockTokens.size(), result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
        assertEquals("ACTIVE", result.get(1).getStatus());
        assertEquals("ACTIVE", result.get(2).getStatus());
        verify(tokenRepository, times(1)).findActiveTokensByConcertId(concertId);
    }
}