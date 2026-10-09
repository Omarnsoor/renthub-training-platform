package com.renthub.dispute;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisputeRepository extends JpaRepository<Dispute,Long>{
 List<Dispute> findByOpenedByOrderByCreatedAtDesc(Long userId);
 List<Dispute> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
