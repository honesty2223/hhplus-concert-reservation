package hhplus.concert.reservation.domain.concertSchedule.service;

import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import hhplus.concert.reservation.domain.concertSchedule.entity.ConcertSchedule;
import hhplus.concert.reservation.domain.concertSchedule.repository.ConcertScheduleRepository;
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
            throw new CoreException(ErrorCode.NO_AVAILABLE_DATES);
        }
        return schedules;
    }

    public ConcertSchedule save(ConcertSchedule concertSchedule) {
        return concertScheduleRepository.save(concertSchedule);
    }
}
