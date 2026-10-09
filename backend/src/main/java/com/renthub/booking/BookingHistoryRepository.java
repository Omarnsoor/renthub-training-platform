package com.renthub.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory,Long>{
 List<BookingHistory> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
