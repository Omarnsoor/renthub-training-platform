package com.renthub.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceRepository extends JpaRepository<MaintenanceRecord,Long>{
 List<MaintenanceRecord> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
 List<MaintenanceRecord> findByAssetTypeAndAssetIdOrderByCreatedAtDesc(String assetType,Long assetId);
}
