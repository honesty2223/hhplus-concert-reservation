package hhplus.concert.reservation.infrastructure.db;

import hhplus.concert.reservation.domain.customer.entity.Customer;
import hhplus.concert.reservation.domain.customer.repository.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerRepositoryImpl implements CustomerRepository {
    private final CustomerJpaRepository customerJpaRepository;

    public CustomerRepositoryImpl(CustomerJpaRepository customerJpaRepository) {
        this.customerJpaRepository = customerJpaRepository;
    }

    @Override
    public Optional<Customer> findById(long customerId) {
        return customerJpaRepository.findById(customerId);
    }

    @Override
    public Optional<Customer> findByIdWithLock(long customerId) {
        return customerJpaRepository.findByIdWithLock(customerId);
    }

    @Override
    public Customer save(Customer customer) {
        return customerJpaRepository.save(customer);
    }
}
