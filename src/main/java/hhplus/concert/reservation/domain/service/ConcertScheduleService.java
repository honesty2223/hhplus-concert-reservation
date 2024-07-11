package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.entity.ConcertSchedule;
import hhplus.concert.reservation.domain.repository.ConcertScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcertScheduleService {

    private final ConcertScheduleRepository concertScheduleRepository;

    public ConcertScheduleService(ConcertScheduleRepository concertScheduleRepository) {
        this.concertScheduleRepository = concertScheduleRepository;
    }

    public List<ConcertSchedule> findByConcertId(long concertId) {
        List<ConcertSchedule> schedules = concertScheduleRepository.findByConcertId(concertId);
        if (schedules.isEmpty()) {
            throw new RuntimeException("해당 콘서트에 예약 가능한 날짜가 없습니다.");
        }
        return schedules;
    }

    public ConcertSchedule save(ConcertSchedule concertSchedule) {
        return concertScheduleRepository.save(concertSchedule);
    }
}
