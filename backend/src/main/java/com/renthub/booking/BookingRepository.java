package com.renthub.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.*;
import java.util.*;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  boolean existsByAssetTypeAndAssetIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(String assetType,Long assetId,Collection<String> statuses,LocalDate newEndDate,LocalDate newStartDate);
  boolean existsByUserIdAndAssetTypeAndAssetIdAndStatusIn(Long userId,String assetType,Long assetId,Collection<String> statuses);
  List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
  List<Booking> findByStatusAndCreatedAtBefore(String status,LocalDateTime createdAt);
  List<Booking> findByStatusAndEndDateLessThanEqual(String status,LocalDate endDate);
}
