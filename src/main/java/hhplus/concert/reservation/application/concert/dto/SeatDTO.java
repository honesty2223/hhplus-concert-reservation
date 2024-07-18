package hhplus.concert.reservation.application.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Long seatId;
    private Long concertScheduleId;
    private Integer seatNumber;
    private Long price;
    private boolean finallyReserved;
    private Long tempAssigneeId;
}
