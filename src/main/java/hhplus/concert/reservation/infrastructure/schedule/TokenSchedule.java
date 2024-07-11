package hhplus.concert.reservation.infrastructure.schedule;

import hhplus.concert.reservation.application.usecase.TokenUsecase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TokenSchedule {

    @Autowired
    private TokenUsecase tokenUsecase;

    @Scheduled(fixedDelay = 10)
    public void manageTokens(){

        int size = 20;
        tokenUsecase.manageTokens(size);
    }

}