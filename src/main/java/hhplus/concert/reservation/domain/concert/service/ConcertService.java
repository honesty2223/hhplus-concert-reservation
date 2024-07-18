package hhplus.concert.reservation.domain.concert.service;

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
                .orElseThrow(() -> new RuntimeException("해당 콘서트를 찾을 수 없습니다 : " + concertId));
    }

    public Concert save(Concert concert) {
        return concertRepository.save(concert);
    }
}
