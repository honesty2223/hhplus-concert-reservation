package hhplus.concert.reservation.domain.customer.repository;

import hhplus.concert.reservation.domain.customer.entity.Customer;

import java.util.Optional;

public interface CustomerRepository {

    Optional<Customer> findById(long customerId);

    Optional<Customer> findByIdWithLock(long customerId);

    Customer save(Customer customer);
}
