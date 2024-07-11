package hhplus.concert.reservation.infrastructure.repository;

import hhplus.concert.reservation.domain.entity.Concert;
import hhplus.concert.reservation.domain.repository.ConcertRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConcertRepositoryImpl implements ConcertRepository {
    private final ConcertJpaRepository concertJpaRepository;

    public ConcertRepositoryImpl(ConcertJpaRepository concertJpaRepository) {
        this.concertJpaRepository = concertJpaRepository;
    }

    @Override
    public List<Concert> findAll() {
        return concertJpaRepository.findAll();
    }

    @Override
    public Optional<Concert> findById(long concertId) {
        return concertJpaRepository.findById(concertId);
    }

    @Override
    public Concert save(Concert concert) {
        return concertJpaRepository.save(concert);
    }
}
