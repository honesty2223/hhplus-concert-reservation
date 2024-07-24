package hhplus.concert.reservation.domain.customer.service;

import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findById(long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CoreException(ErrorCode.USER_NOT_FOUND));
    }

    public Customer findByIdWithLock(long customerId) {
        return customerRepository.findByIdWithLock(customerId)
                .orElseThrow(() -> new CoreException(ErrorCode.USER_NOT_FOUND));
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }
}
