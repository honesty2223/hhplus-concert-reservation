package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.customer.dto.CustomerPointDTO;
import hhplus.concert.reservation.application.customer.usecase.CustomerUsecase;
import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CustomerUsecaseTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerUsecase customerUsecase;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @BeforeEach
    public void setUp() {
        createTime = LocalDateTime.now().minusHours(3);
        updateTime = LocalDateTime.now().minusHours(1);
    }

    @Test
    @DisplayName("잔액 조회 테스트")
    public void getCustomerPointTest() {
        // given
        Customer customer = new Customer(1, "홍길동", 10000, createTime, updateTime);
        customerService.save(customer);

        // when
        CustomerPointDTO result = customerUsecase.getCustomerPoint(customer.getCustomerId());

        // then
        assertNotNull(result);
        assertEquals(customer.getCustomerId(), result.getCustomerId());
        assertEquals(customer.getPoint(), result.getPoint());
    }

    @Test
    @DisplayName("잔액 충전 테스트")
    public void chargePointTest() {
        // given
        Customer customer = new Customer(1, "홍길동", 10000, createTime, updateTime);
        customerService.save(customer);

        // when
        CustomerPointDTO result = customerUsecase.chargePoint(customer.getCustomerId(), 5000);

        // then
        assertNotNull(result);
        assertEquals(customer.getCustomerId(), result.getCustomerId());
        assertEquals(15000, result.getPoint()); // 충전 후 포인트 검증
    }

    @Test
    @DisplayName("잔액 충전 동시성 테스트")
    public void concurrentChargePointTest() throws InterruptedException {
        Customer customer = new Customer(1, "홍길동", 10000, createTime, updateTime);
        customerService.save(customer);

        int numThreads = 10; // 동시에 처리할 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads); // CountDownLatch 초기화

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            long amount = 100 * (i + 1);    // 100 + 200 + 300 + 400 + 500 + 600 + 700 + 800 + 900 + 1000 = 5500

            executorService.submit(() -> {
                try {
                    CustomerPointDTO result = customerUsecase.chargePoint(customer.getCustomerId(), amount);
                    successCount.incrementAndGet();
                    System.out.println("충전 성공 ID : " + result.getCustomerId() + " 잔액 : " + result.getPoint());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown(); // 작업 완료 시 CountDownLatch 감소
                }
            });
        }

        latch.await(); // 모든 스레드가 countDown()을 호출할 때까지 대기

        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            executorService.shutdownNow(); // 작업이 완료되지 않았다면 강제 종료
        }

        System.out.println("성공한 요청 수 : " + successCount.get());
        System.out.println("실패한 요청 수 : " + failCount.get());

        Customer result = customerService.findById(1);
        System.out.println("고객 총 잔액 : " + result.getPoint());

        assertThat(result.getPoint()).isEqualTo(15500); // 기존 잔액(10000) + 충전할 총 금액(5500) = 15500
    }
}
