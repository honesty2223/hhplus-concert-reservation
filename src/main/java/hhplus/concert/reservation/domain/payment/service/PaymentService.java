package hhplus.concert.reservation.domain.payment.service;

import hhplus.concert.reservation.domain.payment.entity.Payment;
import hhplus.concert.reservation.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment findById(long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("해당 결제를 찾을 수 없습니다 : " + paymentId));
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
}
