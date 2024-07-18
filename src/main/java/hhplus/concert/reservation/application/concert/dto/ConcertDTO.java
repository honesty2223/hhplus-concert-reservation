package hhplus.concert.reservation.application.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConcertDTO {
    private Long concertId;
    private String concertName;
}
