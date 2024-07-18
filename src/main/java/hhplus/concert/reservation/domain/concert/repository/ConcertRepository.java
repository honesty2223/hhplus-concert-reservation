package hhplus.concert.reservation.domain.concert.repository;

import hhplus.concert.reservation.domain.concert.entity.Concert;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository {

    List<Concert> findAll();

    Optional<Concert> findById(long concertId);

    Concert save(Concert concert);
}
