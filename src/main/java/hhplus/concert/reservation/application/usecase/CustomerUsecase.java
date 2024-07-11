package hhplus.concert.reservation.application.usecase;

import hhplus.concert.reservation.application.dto.CustomerPointDTO;
import hhplus.concert.reservation.domain.entity.Customer;
import hhplus.concert.reservation.domain.service.CustomerService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CustomerUsecase {

    private final CustomerService customerService;

    public CustomerUsecase(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * 잔액 조회
     *
     * @param customerId 고객 ID
     * @return CustomerPointDTO 고객 ID와 현재 잔액을 포함한 DTO
     */
    public CustomerPointDTO getCustomerPoint(long customerId) {
        Customer customer = customerService.findById(customerId);
        return new CustomerPointDTO(customer.getCustomerId(), customer.getPoint());
    }

    /**
     * 잔액 충전
     *
     * @param customerId 고객 ID
     * @param amount 충전할 금액
     * @return CustomerPointDTO 고객 ID와 현재 잔액을 포함한 DTO
     */
    @Transactional
    public CustomerPointDTO chargePoint(long customerId, long amount) {
        Customer customer = customerService.findByIdWithLock(customerId); // PESSIMISTIC_WRITE 락을 사용
        customer.chargePoint(amount);

        Customer savedCustomer = customerService.save(customer);

        return new CustomerPointDTO(savedCustomer.getCustomerId(), savedCustomer.getPoint());
    }
}
