package hhplus.concert.reservation.infrastructure.db;

import hhplus.concert.reservation.domain.token.entity.Token;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenJpaRepository extends JpaRepository<Token, Long> {

    @Query("SELECT MAX(t.waitNumber) FROM Token t WHERE t.concertId = :concertId")
    Optional<Long> findMaxPositionByConcertId(@Param("concertId") long concertId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Token t WHERE t.customerId = :customerId AND t.concertId = :concertId AND t.status != 'EXPIRED'")
    Token findByCustomerId(@Param("customerId") long customerId, @Param("concertId") long concertId);

    Token findByCustomerIdAndConcertId(long customerId, long concertId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Token t WHERE t.customerId = :customerId AND t.concertId = :concertId")
    Token findByCustomerIdAndConcertIdWithLock(@Param("customerId") long customerId, @Param("concertId") long concertId);

    @Query("SELECT t FROM Token t WHERE t.concertId = :concertId AND t.status = 'PENDING' ORDER BY t.waitNumber ASC")
    List<Token> findPendingTokensByConcertId(@Param("concertId") long concertId);

    @Query("SELECT t FROM Token t WHERE t.concertId = :concertId AND t.status = 'ACTIVE' ORDER BY t.waitNumber DESC")
    List<Token> findActiveTokensByConcertId(@Param("concertId") long concertId);
}
