package hhplus.concert.reservation.presentation.schedule;

import hhplus.concert.reservation.application.token.usecase.TokenUsecase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TokenSchedule {

    @Autowired
    private TokenUsecase tokenUsecase;

    // 30초마다 토큰을 관리합니다
    @Scheduled(fixedRate = 30000)
    public void manageTokens() {
        int size = 1;   // 테스트 코드 실행 확인하려고 임시 수정함
        tokenUsecase.manageTokens(size);
    }

}