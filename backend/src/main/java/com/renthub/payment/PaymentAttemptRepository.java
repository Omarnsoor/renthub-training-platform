package com.renthub.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt,Long>{
 List<PaymentAttempt> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
