package hhplus.concert.reservation.presentation.controller.customer;

import hhplus.concert.reservation.application.customer.dto.CustomerPointDTO;
import hhplus.concert.reservation.application.customer.usecase.CustomerUsecase;
import hhplus.concert.reservation.presentation.controller.customer.request.PointRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "고객 Controller", description = "잔액 조회 API, 잔액 충전 API")
public class CustomerController {

    private final CustomerUsecase customerUsecase;

    public CustomerController(CustomerUsecase customerUsecase) {
        this.customerUsecase = customerUsecase;
    }

    /**
     * 잔액 조회 API
     *
     * @param customerId 고객 ID
     * @return 고객의 잔액 정보를 포함한 응답
     */
    @GetMapping("/customers/{customerId}/point")
    public ResponseEntity<CustomerPointDTO> getCustomer(@PathVariable long customerId) {
        CustomerPointDTO customerPointDTO = customerUsecase.getCustomerPoint(customerId);
        return ResponseEntity.ok(customerPointDTO);
    }

    /**
     * 잔액 충전 API
     *
     * @param customerId 고객 ID
     * @param pointRequest 잔액 충전 요청 정보 (amount)
     * @return 고객의 충전된 잔액 정보를 포함한 응답
     */
    @PatchMapping("/customers/{customerId}/point")
    public ResponseEntity<CustomerPointDTO> chargePoint(@PathVariable long customerId, @RequestBody PointRequest pointRequest) {
        CustomerPointDTO customerPointDTO = customerUsecase.chargePoint(customerId, pointRequest.getAmount());
        return ResponseEntity.ok(customerPointDTO);
    }
}
