package hhplus.concert.reservation.domain.service;

import hhplus.concert.reservation.domain.seat.entity.Seat;
import hhplus.concert.reservation.domain.seat.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

// 테스트 데이터를 빠르게 적재하기 위한 용도
public class MultiThread implements Runnable{

    private final long pk;
    private final int concertScheduledId;
    private final int seatNumber;

    @Autowired
    private SeatService seatService;

    public MultiThread(long pk, int concertScheduledId, int number, SeatService seatService) {
        this.pk = pk;
        this.concertScheduledId = concertScheduledId;
        this.seatNumber = number;
        this.seatService = seatService;
    }

    @Override
    public void run() {
        LocalDateTime createTime = LocalDateTime.now();
        Seat newSeat = null;
        newSeat = new Seat(pk, concertScheduledId, seatNumber, 7000, false, 0, null, createTime, null);
        seatService.save(newSeat);
    }
}
