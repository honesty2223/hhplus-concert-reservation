package hhplus.concert.reservation.domain.concert.service;

import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import hhplus.concert.reservation.domain.concert.entity.Concert;
import hhplus.concert.reservation.domain.concert.repository.ConcertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    public List<Concert> findAll() {
        return concertRepository.findAll();
    }

    public Concert findById(long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new CoreException(ErrorCode.CONCERT_NOT_FOUND));
    }

    public Concert save(Concert concert) {
        return concertRepository.save(concert);
    }
}
