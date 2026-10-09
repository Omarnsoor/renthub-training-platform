package com.renthub.availability;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name="RH_AVAILABILITY_BLOCKS")
public class AvailabilityBlock {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="avail_block_seq") @SequenceGenerator(name="avail_block_seq",sequenceName="RH_AVAIL_BLOCK_SEQ",allocationSize=1) private Long id;
 private String assetType; private Long assetId; private LocalDate startDate; private LocalDate endDate; private String reason; private String blockType="OWNER_BLOCK"; private Long createdBy; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public String getAssetType(){return assetType;} public void setAssetType(String v){assetType=v;} public Long getAssetId(){return assetId;} public void setAssetId(Long v){assetId=v;}
 public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;} public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;} public String getReason(){return reason;} public void setReason(String v){reason=v;}
 public String getBlockType(){return blockType;} public void setBlockType(String v){blockType=v;} public Long getCreatedBy(){return createdBy;} public void setCreatedBy(Long v){createdBy=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
