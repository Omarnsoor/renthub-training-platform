package com.renthub.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund,Long>{
 List<Refund> findByUserIdOrderByCreatedAtDesc(Long userId);
 List<Refund> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
 boolean existsByBookingIdAndStatusIn(Long bookingId,java.util.Collection<String> statuses);
}
