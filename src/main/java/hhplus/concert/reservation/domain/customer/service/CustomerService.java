package hhplus.concert.reservation.domain.customer.service;

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
                .orElseThrow(() -> new RuntimeException("해당 고객을 찾을 수 없습니다 : " + customerId));
    }

    public Customer findByIdWithLock(long customerId) {
        return customerRepository.findByIdWithLock(customerId)
                .orElseThrow(() -> new RuntimeException("해당 고객을 찾을 수 없습니다 : " + customerId));
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }
}
