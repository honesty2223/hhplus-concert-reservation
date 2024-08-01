package hhplus.concert.reservation.application.token.usecase;

import hhplus.concert.reservation.application.token.dto.RedisTokenDTO;
import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.service.CustomerService;
import hhplus.concert.reservation.domain.token.entity.RedisToken;
import hhplus.concert.reservation.domain.token.service.QueueService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class QueueUsecase {

    private final QueueService queueService;
    private final CustomerService customerService;

    public QueueUsecase(QueueService queueService, CustomerService customerService) {
        this.queueService = queueService;
        this.customerService = customerService;
    }

    private RedisTokenDTO convertToRedisTokenDTO(RedisToken token) {
        return new RedisTokenDTO(
                token.getCustomerId(),
                token.getTokenID(),
                token.getRank()
        );
    }

    /**
     * 토큰 생성 (콘서트 대기열에 참가)
     *
     * @param customerId 고객 ID
     * @return 생성된 토큰(대기열) 정보를 담은 RedisTokenDTO 객체
     */
    public RedisTokenDTO generateNewToken(long customerId) {
        Customer customer = customerService.findById(customerId);
        return convertToRedisTokenDTO(queueService.generateNewToken(customer.getCustomerId()));
    }

    /**
     * 대기열 조회
     *
     * @param customerId 고객 ID
     * @return 토큰 정보(대기열) 정보를 담은 RedisTokenDTO 객체
     */
    public RedisTokenDTO checkToken(long customerId) {
        Customer customer = customerService.findById(customerId);
        return convertToRedisTokenDTO(queueService.checkToken(customer.getCustomerId()));
    }

    /**
     * 토큰 활성화 여부 조회
     *
     * @param tokenId 토큰 ID
     * @return 토큰의 활성화 여부
     */
    public boolean isActiveToken(UUID tokenId){
        return queueService.isActiveToken(tokenId);
    }

    /**
     * 스케줄러가 자동으로 토큰 상태 관리
     *
     * @param size 대기열의 정원 크기
     */
    public void manageTokens(int size) {
        queueService.expireToken();
        queueService.activeToken(size);
    }

}
