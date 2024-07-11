package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.dto.TokenDTO;
import hhplus.concert.reservation.domain.entity.Concert;
import hhplus.concert.reservation.domain.entity.Token;
import hhplus.concert.reservation.domain.service.ConcertService;
import hhplus.concert.reservation.domain.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//@Transactional
public class TokenUsecase {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private ConcertService concertService;

    public TokenUsecase(TokenService tokenService, ConcertService concertService) {
        this.tokenService = tokenService;
        this.concertService = concertService;
    }

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

    public TokenDTO issueToken(long customerId, long concertId) {

        Token targetToken = tokenService.issueToken(customerId, concertId);

        return convertToTokenDTO(tokenService.issueToken(customerId, concertId));
    }

    public void manageTokens(int size){

        List<Concert> concertList = concertService.findAll();

        for(Concert concert : concertList){
            tokenService.expireToken(concert.getConcertId());
            tokenService.activeToken(concert.getConcertId(), size);
        }
    }
}