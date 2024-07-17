package hhplus.concert.reservation.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPointDTO {
    private Long customerId;
    private Long point;
}
