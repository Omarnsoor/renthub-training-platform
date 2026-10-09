package com.renthub.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingPriceAdjustmentRepository extends JpaRepository<BookingPriceAdjustment,Long>{
  List<BookingPriceAdjustment> findByBookingIdOrderByIdAsc(Long bookingId);
}
