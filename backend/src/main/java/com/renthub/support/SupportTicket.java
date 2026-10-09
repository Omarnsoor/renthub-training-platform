package com.renthub.support;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="RH_SUPPORT_TICKETS")
public class SupportTicket {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ticket_seq") @SequenceGenerator(name="ticket_seq",sequenceName="RH_TICKET_SEQ",allocationSize=1) private Long id;
 private Long userId; private Long bookingId; private String category; private String subject; private String description; private String priority="NORMAL"; private String status="OPEN"; private Long assignedTo; private String resolution;
 private LocalDateTime createdAt=LocalDateTime.now(); private LocalDateTime updatedAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;} public Long getBookingId(){return bookingId;} public void setBookingId(Long v){bookingId=v;}
 public String getCategory(){return category;} public void setCategory(String v){category=v;} public String getSubject(){return subject;} public void setSubject(String v){subject=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public String getPriority(){return priority;} public void setPriority(String v){priority=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public Long getAssignedTo(){return assignedTo;} public void setAssignedTo(Long v){assignedTo=v;}
 public String getResolution(){return resolution;} public void setResolution(String v){resolution=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
