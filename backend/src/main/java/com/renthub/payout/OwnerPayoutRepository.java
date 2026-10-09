package com.renthub.payout;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OwnerPayoutRepository extends JpaRepository<OwnerPayout,Long>{
 List<OwnerPayout> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
 Optional<OwnerPayout> findByBookingId(Long bookingId);
}
