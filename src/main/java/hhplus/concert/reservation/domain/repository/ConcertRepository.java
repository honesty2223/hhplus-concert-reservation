package hhplus.concert.reservation.domain.repository;

import hhplus.concert.reservation.domain.entity.Concert;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository {

    List<Concert> findAll();

    Optional<Concert> findById(long concertId);

    Concert save(Concert concert);
}
