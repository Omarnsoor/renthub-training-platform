package com.renthub.availability;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock,Long>{
 boolean existsByAssetTypeAndAssetIdAndStartDateLessThanAndEndDateGreaterThan(String assetType,Long assetId,LocalDate newEnd,LocalDate newStart);
 List<AvailabilityBlock> findByAssetTypeAndAssetIdOrderByStartDateDesc(String assetType,Long assetId);
}
