package hhplus.concert.reservation.application.token.usecase;

import hhplus.concert.reservation.application.token.dto.TokenDTO;
import hhplus.concert.reservation.domain.concert.entity.Concert;
import hhplus.concert.reservation.domain.concert.service.ConcertService;
import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.service.CustomerService;
import hhplus.concert.reservation.domain.token.entity.Token;
import hhplus.concert.reservation.domain.token.service.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TokenUsecase {

    private final TokenService tokenService;
    private final CustomerService customerService;
    private final ConcertService concertService;

    public TokenUsecase(TokenService tokenService, CustomerService customerService, ConcertService concertService) {
        this.tokenService = tokenService;
        this.customerService = customerService;
        this.concertService = concertService;
    }

    /**
     * Token 객체를 TokenDTO로 변환합니다.
     */
    private TokenDTO convertToTokenDTO(Token token) {
        return new TokenDTO(
                token.getTokenId(),
                token.getConcertId(),
                token.getCustomerId(),
                token.getWaitNumber(),
                token.getStatus(),
                token.getCreatedAt(),
                token.getUpdatedAt()
        );
    }

    /**
     * 토큰 생성 (콘서트 대기열에 참가)
     *
     * @param customerId 고객 ID
     * @param concertId 콘서트 ID
     * @return 생성된 토큰(대기열) 정보를 담은 TokenDTO 객체
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TokenDTO generateNewToken(long customerId, long concertId) {
        Customer customer = customerService.findById(customerId);
        Concert concert = concertService.findById(concertId);
        return convertToTokenDTO(tokenService.generateNewToken(customer.getCustomerId(), concert.getConcertId()));
    }

    /**
     * 콘서트 대기열 조회
     *
     * @param customerId 고객 ID
     * @param concertId 콘서트 ID
     * @return 토큰 정보(대기열)를 담은 TokenDTO 객체
     */
    @Transactional
    public TokenDTO checkToken(long customerId, long concertId) {
        Customer customer = customerService.findById(customerId);
        Concert concert = concertService.findById(concertId);
        return convertToTokenDTO(tokenService.checkToken(customer.getCustomerId(), concert.getConcertId()));
    }

    /**
     * 토큰 활성화 여부 조회
     *
     * @param tokenId 토큰 ID
     * @return 토큰의 활성화 여부
     */
    public boolean isActiveToken(long tokenId){
        return tokenService.isActiveToken(tokenId);
    }

    /**
     * 스케줄러가 자동으로 토큰 상태 관리
     *
     * @param size 대기열의 정원 크기
     */
    public void manageTokens(int size) {

        List<Concert> concertList = concertService.findAll();

        for(Concert concert : concertList) {
            tokenService.expireToken(concert.getConcertId());
            tokenService.activeToken(concert.getConcertId(), size);
        }
    }
}
