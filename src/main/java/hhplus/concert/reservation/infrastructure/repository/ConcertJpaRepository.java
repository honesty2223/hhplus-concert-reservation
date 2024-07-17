package hhplus.concert.reservation.infrastructure.repository;

import hhplus.concert.reservation.domain.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {
}
