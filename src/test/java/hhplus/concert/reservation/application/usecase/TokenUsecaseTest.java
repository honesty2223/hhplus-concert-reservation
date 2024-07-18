package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.token.dto.TokenDTO;
import hhplus.concert.reservation.application.token.usecase.TokenUsecase;
import hhplus.concert.reservation.domain.concert.entity.Concert;
import hhplus.concert.reservation.domain.concert.service.ConcertService;
import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TokenUsecaseTest {

    @Autowired
    private TokenUsecase tokenUsecase;

    @Autowired
    private ConcertService concertService;

    @Autowired
    private CustomerService customerService;

    @Test
    @DisplayName("토큰 발급 동시 접근 테스트")
    public void generateNewToken() throws Exception {

        // given
        int expectedTokens = 20;

        // given Concert
        LocalDateTime createTime = LocalDateTime.now().minusHours(3);
        LocalDateTime updateTime = LocalDateTime.now().minusHours(1);
        concertService.save(new Concert(1, "이무진 콘서트", createTime, updateTime));

        // given Customer
        int totalCustomer = 100;
        for(int i = 0; i < totalCustomer; i++){
            customerService.save(new Customer(i, "" + i, 0, createTime, updateTime));
        }

        // given MultiThread
        int concurrentThreads = 100; // 동시 실행할 쓰레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(totalCustomer);
        CountDownLatch latch = new CountDownLatch(totalCustomer);

        // when customer polling token
        // 각 쓰레드가 실행할 작업
        Runnable task = () -> {
            try {
                Random random = new Random();
                int randomCustomerId = random.nextInt(totalCustomer);
                TokenDTO generateResult = tokenUsecase.generateNewToken(randomCustomerId, 1L);
                System.out.println("customerId : " + generateResult.getCustomerId() + " tokenId : " + generateResult.getTokenId() + " number : " + generateResult.getWaitNumber());
                assertNotNull(generateResult);
                assertEquals(randomCustomerId, generateResult.getCustomerId());
                assertEquals(1, generateResult.getConcertId());
                assertEquals("PENDING", generateResult.getStatus());

            } finally {
                latch.countDown();
            }
        };

        // 동시에 실행할 쓰레드 수만큼 작업 제출 // 토큰 만료 확인 위해 3번 실행
        for(int c = 0; c < 3; c++){
            IntStream.range(0, concurrentThreads).forEach(i -> executorService.submit(task));
            // 토큰 만료를 위해 발급 후 30초간 sleep
            TimeUnit.SECONDS.sleep(10);
        }

        // 모든 쓰레드가 완료될 때까지 기다림
        latch.await();
        executorService.shutdown();
        if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
            executorService.shutdownNow(); // 작업이 완료되지 않았다면 강제 종료
        }
    }

    @Test
    @DisplayName("콘서트 대기열 조회 테스트")
    public void checkToken() {
        // given
        long customerId = 1;
        long concertId = 1;
        customerService.save(new Customer(customerId, "홍길동", 0, LocalDateTime.now(), null));
        concertService.save(new Concert(1, "이무진 콘서트", LocalDateTime.now(), LocalDateTime.now()));
        TokenDTO generateToken = tokenUsecase.generateNewToken(customerId, concertId);

        // when
        TokenDTO result = tokenUsecase.checkToken(customerId, concertId);

        // then
        assertEquals(generateToken.getTokenId(), result.getTokenId());
        assertEquals(generateToken.getCustomerId(), result.getCustomerId());
        assertEquals(generateToken.getConcertId(), result.getConcertId());
        assertEquals(generateToken.getStatus(), result.getStatus());
    }
}
