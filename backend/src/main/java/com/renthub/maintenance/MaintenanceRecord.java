package com.renthub.maintenance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Table(name="RH_MAINTENANCE")
public class MaintenanceRecord {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="maintenance_seq") @SequenceGenerator(name="maintenance_seq",sequenceName="RH_MAINTENANCE_SEQ",allocationSize=1) private Long id;
 private String assetType; private Long assetId; private Long ownerId; private Long availabilityBlockId; private String maintenanceType; private String description; private LocalDate startDate; private LocalDate endDate; private BigDecimal cost=BigDecimal.ZERO; private String status="SCHEDULED"; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public String getAssetType(){return assetType;} public void setAssetType(String v){assetType=v;} public Long getAssetId(){return assetId;} public void setAssetId(Long v){assetId=v;} public Long getOwnerId(){return ownerId;} public void setOwnerId(Long v){ownerId=v;} public Long getAvailabilityBlockId(){return availabilityBlockId;} public void setAvailabilityBlockId(Long v){availabilityBlockId=v;}
 public String getMaintenanceType(){return maintenanceType;} public void setMaintenanceType(String v){maintenanceType=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;} public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
 public BigDecimal getCost(){return cost;} public void setCost(BigDecimal v){cost=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
