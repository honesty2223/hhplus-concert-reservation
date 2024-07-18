package hhplus.concert.reservation.domain.reservation.repository;

import hhplus.concert.reservation.domain.reservation.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(long reservationId);

    List<Reservation> findByStatusAndReservationTimeBefore(String status, LocalDateTime cutoffTime);

    Reservation save(Reservation reservation);
}
