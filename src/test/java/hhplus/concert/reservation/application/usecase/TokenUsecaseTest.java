package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.dto.TokenDTO;
import hhplus.concert.reservation.domain.entity.Concert;
import hhplus.concert.reservation.domain.entity.Customer;
import hhplus.concert.reservation.domain.repository.TokenRepository;
import hhplus.concert.reservation.domain.service.ConcertService;
import hhplus.concert.reservation.domain.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

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
@ActiveProfiles("test")
@Execution(ExecutionMode.CONCURRENT)
@DirtiesContext
public class TokenUsecaseTest {

    @Autowired
    private TokenUsecase tokenUsecase;

    @Autowired
    private ConcertService concertService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    @DisplayName("토큰 발급, 토큰 조회, 토큰 만료, 토큰 활성화 동시 접근 테스트")
    public void issueToken() throws Exception {

        // given
        int expectedTokens = 20;

        // given Concert
        LocalDateTime createTime = LocalDateTime.now().minusHours(3);
        LocalDateTime updateTime = LocalDateTime.now().minusHours(1);
        concertService.save(new Concert(1, "이무진 콘서트", createTime, updateTime));
        concertService.save(new Concert(2, "소수빈 콘서트", createTime, updateTime));
        concertService.save(new Concert(3, "뉴진스 콘서트", createTime, updateTime));

        // given Customer
        int totalCustomer = 100;
        for(int i = 0; i < totalCustomer; i++){
            synchronized (customerService) {
                customerService.save(new Customer(i, "" + i, 0, createTime, updateTime));
            }
        }

        // given MultiThread
        int concurrentThreads = 1000; // 동시 실행할 쓰레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(totalCustomer);
        CountDownLatch latch = new CountDownLatch(totalCustomer);

        // when customer polling token
        // 각 쓰레드가 실행할 작업
        Runnable task = () -> {
            try {
                Random random = new Random();
                int randomCustomerId = random.nextInt(totalCustomer);
                TokenDTO result = tokenUsecase.issueToken(randomCustomerId, 1L);

                assertNotNull(result);
                assertEquals(randomCustomerId, result.getCustomerId());
                assertEquals(1L, result.getConcertId());
                assertEquals("PENDING", result.getStatus());

                // 토큰 만료를 위해 발급 후 1초간 sleep
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        };

        // 동시에 실행할 쓰레드 수만큼 작업 제출
        IntStream.range(0, concurrentThreads).forEach(i -> executorService.submit(task));

        // 모든 쓰레드가 완료될 때까지 기다림
        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

    }
}