package hhplus.concert.reservation.infrastructure;

import hhplus.concert.reservation.domain.token.entity.Token;
import hhplus.concert.reservation.domain.token.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
@Repository
public class TokenRepositoryImpl implements TokenRepository {

    private final TokenJpaRepository tokenJpaRepository;

    @Autowired
    public TokenRepositoryImpl(TokenJpaRepository tokenJpaRepository) {
        this.tokenJpaRepository = tokenJpaRepository;
    }

    @Override
    public Optional<Long> findMaxPositionByConcertId(long concertId) {
        return tokenJpaRepository.findMaxPositionByConcertId(concertId);
    }

    @Override
    public Token save(Token newToken) {
        return tokenJpaRepository.save(newToken);
    }

    @Override
    public Optional<Token> findById(long tokenId) {
        return tokenJpaRepository.findById(tokenId);
    }

    @Override
    public Token findByCustomerId(long customerId, long concertId) {
        return tokenJpaRepository.findByCustomerId(customerId, concertId);
    }

    @Override
    public Token findByCustomerIdAndConcertId(long customerId, long concertId) {
        return tokenJpaRepository.findByCustomerIdAndConcertId(customerId, concertId);
    }

    @Override
    public Token findByCustomerIdAndConcertIdWithLock(long customerId, long concertId) {
        return tokenJpaRepository.findByCustomerIdAndConcertIdWithLock(customerId, concertId);
    }

    @Override
    public List<Token> findPendingTokensByConcertId(long concertId) {
        return tokenJpaRepository.findPendingTokensByConcertId(concertId);
    }

    @Override
    public List<Token> findActiveTokensByConcertId(long concertId) {
        return tokenJpaRepository.findActiveTokensByConcertId(concertId);
    }
}
