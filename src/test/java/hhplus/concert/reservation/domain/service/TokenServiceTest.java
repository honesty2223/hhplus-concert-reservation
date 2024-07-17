package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.entity.Token;
import hhplus.concert.reservation.domain.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    @DisplayName("토큰 생성 테스트")
    public void issueToken_generate() {
        // given
        long customerId = 1L;
        long concertId = 1L;
        Optional<Long> expectedWaitNumber = Optional.of(1L);
        long nextWaitNumber = expectedWaitNumber.orElse(0L) + 1;
        Token givenToken = new Token(1L, concertId, customerId, nextWaitNumber, "PENDING", now, null);

        // given Mock
        when(tokenRepository.findByCustomerId(customerId, concertId)).thenReturn(null);
        when(tokenRepository.findMaxPositionByConcertId(concertId)).thenReturn(expectedWaitNumber);
        when(tokenRepository.save(any(Token.class))).thenReturn(givenToken);

        // when
        Token actualToken = tokenService.issueToken(customerId, concertId);

        // then
        verify(tokenRepository, times(1)).findByCustomerId(customerId, concertId);
        verify(tokenRepository, times(1)).findMaxPositionByConcertId(concertId);
        verify(tokenRepository, times(1)).save(any(Token.class));
        assertEquals(givenToken, actualToken);
    }

    @Test
    @DisplayName("토큰 폴링 테스트")
    public void issueToken_polling() {
        // given
        long customerId = 2L;
        long concertId = 1L;
        Token givenToken = new Token(2L, concertId, customerId, 2L, "PENDING", now, null);
        Token lastActiveToken = new Token(1L, concertId, 1L, 1, "ACTIVE", now.minusMinutes(10), now);

        // given Mock
        when(tokenRepository.findByCustomerId(customerId, concertId)).thenReturn(givenToken);
        when(tokenRepository.findActiveTokensByConcertId(concertId)).thenReturn(List.of(lastActiveToken));

        // when
        Token actualToken = tokenService.issueToken(customerId, concertId);

        // then
        verify(tokenRepository, never()).findMaxPositionByConcertId(concertId);
        verify(tokenRepository, never()).save(any(Token.class));
        assertEquals(givenToken.getTokenId(), actualToken.getTokenId());
        assertEquals(givenToken.getCustomerId(), actualToken.getCustomerId());
        assertEquals(givenToken.getConcertId(), actualToken.getConcertId());
        assertEquals(1, actualToken.getWaitNumber());
        assertEquals(givenToken.getStatus(), actualToken.getStatus());
        assertEquals(givenToken.getCreatedAt(), actualToken.getCreatedAt());
        assertNull(actualToken.getUpdatedAt());
    }

    @Test
    @DisplayName("토큰 ID로 토큰 찾기 테스트")
    public void findById() {
        // given
        long tokenId = 1L;
        Optional<Token> givenToken = Optional.of(new Token(tokenId, 1L, 1L, 1, "ACTIVE", now, now));

        // given Mock
        when(tokenRepository.findById(tokenId)).thenReturn(givenToken);

        // when
        Optional<Token> actualToken = tokenService.findById(tokenId);

        // then
        verify(tokenRepository, times(1)).findById(tokenId);
        assertEquals(givenToken, actualToken);
    }

    @Test
    @DisplayName("고객 ID로 토큰 찾기 테스트")
    public void findByCustomerId() {
        // given
        long customerId = 1L;
        long concertId = 1L;
        Token givenToken = new Token(1L, concertId, customerId, 1, "PENDING", now, null);

        // given Mock
        when(tokenRepository.findByCustomerId(customerId, concertId)).thenReturn(givenToken);

        // when
        Token actualToken = tokenService.findByCustomerId(customerId, concertId);

        // then
        verify(tokenRepository, times(1)).findByCustomerId(customerId, concertId);
        assertEquals(givenToken, actualToken);
    }

    @Test
    @DisplayName("토큰 활성화 테스트")
    public void activeToken() {
        // given
        long concertId = 1L;
        int size = 3;
        List<Token> waitingTokens = new ArrayList<>();
        Token givenPendingToken1 = new Token(1L, concertId, 1L, 1L, "PENDING", now.minusMinutes(3), null);
        Token givenPendingToken2 = new Token(2L, concertId, 2L, 2L, "PENDING", now.minusMinutes(2), null);
        Token givenPendingToken3 = new Token(3L, concertId, 3L, 3L, "PENDING", now.minusMinutes(1), null);
        waitingTokens.add(givenPendingToken1);
        waitingTokens.add(givenPendingToken2);
        waitingTokens.add(givenPendingToken3);
        List<Token> ativeTokens = new ArrayList<>();

        // given Mock
        when(tokenRepository.findPendingTokensByConcertId(concertId)).thenReturn(waitingTokens);
        when(tokenRepository.findActiveTokensByConcertId(concertId)).thenReturn(ativeTokens);

        // when
        List<Token> actualToken = tokenService.activeToken(concertId, size);

        // then
        verify(tokenRepository, times(1)).findPendingTokensByConcertId(concertId);
        verify(tokenRepository, times(1)).findActiveTokensByConcertId(concertId);
        verify(tokenRepository, times(3)).save(any(Token.class));
        assertEquals(size, actualToken.size());
        assertEquals("ACTIVE", actualToken.get(0).getStatus());
        assertEquals("ACTIVE", actualToken.get(1).getStatus());
        assertEquals("ACTIVE", actualToken.get(2).getStatus());
    }

    @Test
    @DisplayName("토큰 만료 테스트")
    public void expireToken(){
        // given
        long concertId = 1L;
        List<Token> ativeTokens = new ArrayList<>();
        Token givenActiveToken1 = new Token(1L, concertId, 1L, 1L, "ACTIVE", now.minusMinutes(60), now.minusMinutes(50));
        Token givenActiveToken2 = new Token(2L, concertId, 2L, 2L, "ACTIVE", now.minusMinutes(50), now.minusMinutes(40));
        Token givenActiveToken3 = new Token(3L, concertId, 3L, 3L, "ACTIVE", now.minusMinutes(40), now.minusMinutes(30));
        ativeTokens.add(givenActiveToken1);
        ativeTokens.add(givenActiveToken2);
        ativeTokens.add(givenActiveToken3);

        // given Mock
        when(tokenRepository.findActiveTokensByConcertId(concertId)).thenReturn(ativeTokens);

        // when
        List<Token> actualToken = tokenService.expireToken(concertId);

        // then
        verify(tokenRepository, times(1)).findActiveTokensByConcertId(concertId);
        verify(tokenRepository, times(3)).save(any(Token.class));
        assertEquals(3, actualToken.size());
        assertEquals("EXPIRED", actualToken.get(0).getStatus());
        assertEquals("EXPIRED", actualToken.get(1).getStatus());
        assertEquals("EXPIRED", actualToken.get(2).getStatus());
    }
}