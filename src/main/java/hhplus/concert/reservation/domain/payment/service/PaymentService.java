package hhplus.concert.reservation.domain.payment.service;

import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
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
                .orElseThrow(() -> new CoreException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    public Payment findByReservationId(long reservationId) {
        return paymentRepository.findByReservationId(reservationId);
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
}
