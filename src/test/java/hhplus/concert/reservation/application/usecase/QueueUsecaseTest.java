package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.token.dto.RedisTokenDTO;
import hhplus.concert.reservation.application.token.usecase.QueueUsecase;
import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class QueueUsecaseTest {

    @Autowired
    private QueueUsecase queueUsecase;

    @Autowired
    private CustomerService customerService;

    @Test
    @DisplayName("레디스 대기열 토큰 발급 테스트")
    public void generateTokenWithRedis(){
        LocalDateTime createTime = LocalDateTime.now().minusHours(3);
        LocalDateTime updateTime = LocalDateTime.now().minusHours(1);
        customerService.save(new Customer(1, "홍길동", 0, createTime, updateTime));
        customerService.save(new Customer(2, "김가나", 0, createTime, updateTime));
        customerService.save(new Customer(3, "고양이", 0, createTime, updateTime));

        RedisTokenDTO generateResult = queueUsecase.generateNewToken(1);
        assertNotNull(generateResult);
        assertEquals(1, generateResult.getCustomerId());
    }

    @Test
    @DisplayName("레디스 대기열 조회 테스트")
    public void checkTokenWithRedis() {
        LocalDateTime createTime = LocalDateTime.now().minusHours(3);
        LocalDateTime updateTime = LocalDateTime.now().minusHours(1);
        customerService.save(new Customer(1, "홍길동", 0, createTime, updateTime));

        RedisTokenDTO generateResult = queueUsecase.generateNewToken(1);

        RedisTokenDTO result = queueUsecase.checkToken(1);
        assertNotNull(result);
        assertEquals(1, result.getCustomerId());
        System.out.println("고객 ID : " + result.getCustomerId() + " 토큰 ID : " + result.getTokenID() + " 대기순서 : " + result.getRank());
    }

    @Test
    @DisplayName("레디스 토큰 활성화/만료 테스트")
    public void manageTokens() throws InterruptedException {
        LocalDateTime createTime = LocalDateTime.now().minusHours(3);
        LocalDateTime updateTime = LocalDateTime.now().minusHours(1);
        customerService.save(new Customer(1, "홍길동", 0, createTime, updateTime));
        customerService.save(new Customer(2, "고양이", 0, createTime, updateTime));
        RedisTokenDTO generateResult = queueUsecase.generateNewToken(1);
        RedisTokenDTO generateResult2 = queueUsecase.generateNewToken(2);
        System.out.println("=========== 생성 완료 =================");
        RedisTokenDTO result = queueUsecase.checkToken(1);
        System.out.println("고객 1 ID : " + result.getCustomerId() + " 토큰 ID : " + result.getTokenID() + " 대기순서 : " + result.getRank());
        RedisTokenDTO result2 = queueUsecase.checkToken(2);
        System.out.println("고객 2 ID : " + result2.getCustomerId() + " 토큰 ID : " + result2.getTokenID() + " 대기순서 : " + result2.getRank());
        System.out.println("=========== 조회 완료 =================");
        assertNotNull(result);
        assertNotNull(result2);
        queueUsecase.manageTokens(1);
        Thread.sleep(60000);
        queueUsecase.manageTokens(1);
    }

    @Test
    @DisplayName("레디스 토큰 활성화 여부 테스트")
    public void isActiveTokenWithRedis() {
        LocalDateTime createTime = LocalDateTime.now().minusHours(3);
        LocalDateTime updateTime = LocalDateTime.now().minusHours(1);
        customerService.save(new Customer(1, "홍길동", 0, createTime, updateTime));
        customerService.save(new Customer(2, "고양이", 0, createTime, updateTime));
        RedisTokenDTO generateResult = queueUsecase.generateNewToken(1);
        RedisTokenDTO generateResult2 = queueUsecase.generateNewToken(2);
        System.out.println("=========== 생성 완료 =================");
        RedisTokenDTO result = queueUsecase.checkToken(1);
        System.out.println("고객 1 ID : " + result.getCustomerId() + " 토큰 ID : " + result.getTokenID() + " 대기순서 : " + result.getRank());
        RedisTokenDTO result2 = queueUsecase.checkToken(2);
        System.out.println("고객 2 ID : " + result2.getCustomerId() + " 토큰 ID : " + result2.getTokenID() + " 대기순서 : " + result2.getRank());
        System.out.println("=========== 조회 완료 =================");
        assertNotNull(result);
        assertNotNull(result2);
        queueUsecase.manageTokens(1);
        System.out.println("활성화 여부 : " + queueUsecase.isActiveToken(result.getTokenID()));
        System.out.println("활성화 여부 : " + queueUsecase.isActiveToken(result2.getTokenID()));
    }
}
