package hhplus.concert.reservation.infrastructure.concert;

import hhplus.concert.reservation.domain.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {
}
