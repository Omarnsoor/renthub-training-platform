package com.renthub.booking;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="RH_BOOKING_HISTORY")
public class BookingHistory {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="booking_history_seq") @SequenceGenerator(name="booking_history_seq",sequenceName="RH_BOOKING_HISTORY_SEQ",allocationSize=1) private Long id;
 private Long bookingId; private String oldStatus; private String newStatus; private Long changedBy; private String reason; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getBookingId(){return bookingId;} public void setBookingId(Long v){bookingId=v;} public String getOldStatus(){return oldStatus;} public void setOldStatus(String v){oldStatus=v;} public String getNewStatus(){return newStatus;} public void setNewStatus(String v){newStatus=v;}
 public Long getChangedBy(){return changedBy;} public void setChangedBy(Long v){changedBy=v;} public String getReason(){return reason;} public void setReason(String v){reason=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
