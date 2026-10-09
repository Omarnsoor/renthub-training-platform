package com.renthub.payout;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="RH_OWNER_PAYOUTS")
public class OwnerPayout {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="payout_seq") @SequenceGenerator(name="payout_seq",sequenceName="RH_PAYOUT_SEQ",allocationSize=1) private Long id;
 private Long ownerId; private Long bookingId; private BigDecimal grossAmount; private BigDecimal platformFee; private BigDecimal payoutAmount; private String status="READY"; private LocalDateTime createdAt=LocalDateTime.now(); private LocalDateTime paidAt;
 public Long getId(){return id;} public Long getOwnerId(){return ownerId;} public void setOwnerId(Long v){ownerId=v;} public Long getBookingId(){return bookingId;} public void setBookingId(Long v){bookingId=v;}
 public BigDecimal getGrossAmount(){return grossAmount;} public void setGrossAmount(BigDecimal v){grossAmount=v;} public BigDecimal getPlatformFee(){return platformFee;} public void setPlatformFee(BigDecimal v){platformFee=v;} public BigDecimal getPayoutAmount(){return payoutAmount;} public void setPayoutAmount(BigDecimal v){payoutAmount=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getPaidAt(){return paidAt;} public void setPaidAt(LocalDateTime v){paidAt=v;}
}
