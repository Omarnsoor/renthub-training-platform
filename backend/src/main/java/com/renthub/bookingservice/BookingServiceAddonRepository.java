package com.renthub.bookingservice;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingServiceAddonRepository extends JpaRepository<BookingServiceAddon,Long> {
  List<BookingServiceAddon> findByBookingId(Long bookingId);
  boolean existsByBookingIdAndServiceId(Long bookingId, Long serviceId);
  Optional<BookingServiceAddon> findByBookingIdAndServiceId(Long bookingId, Long serviceId);
}
