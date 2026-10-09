package com.renthub.dispute;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="RH_DISPUTES")
public class Dispute {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="dispute_seq") @SequenceGenerator(name="dispute_seq",sequenceName="RH_DISPUTE_SEQ",allocationSize=1) private Long id;
 private Long bookingId; private Long openedBy; private String category; private String description; private String status="OPEN"; private String resolution; private LocalDateTime createdAt=LocalDateTime.now(); private LocalDateTime resolvedAt;
 public Long getId(){return id;} public Long getBookingId(){return bookingId;} public void setBookingId(Long v){bookingId=v;} public Long getOpenedBy(){return openedBy;} public void setOpenedBy(Long v){openedBy=v;}
 public String getCategory(){return category;} public void setCategory(String v){category=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getResolution(){return resolution;} public void setResolution(String v){resolution=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getResolvedAt(){return resolvedAt;} public void setResolvedAt(LocalDateTime v){resolvedAt=v;}
}
