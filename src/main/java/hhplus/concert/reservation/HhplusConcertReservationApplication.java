package hhplus.concert.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HhplusConcertReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(HhplusConcertReservationApplication.class, args);
	}

}
