package com.renthub.refund;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="RH_REFUNDS")
public class Refund {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="refund_seq") @SequenceGenerator(name="refund_seq",sequenceName="RH_REFUND_SEQ",allocationSize=1) private Long id;
 private Long bookingId; private Long userId; private Long paymentId; private BigDecimal requestedAmount; private BigDecimal approvedAmount; private BigDecimal feeAmount=BigDecimal.ZERO;
 private String reason; private String status="REQUESTED"; private String reviewNote; private LocalDateTime createdAt=LocalDateTime.now(); private LocalDateTime resolvedAt;
 public Long getId(){return id;} public Long getBookingId(){return bookingId;} public void setBookingId(Long v){bookingId=v;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
 public Long getPaymentId(){return paymentId;} public void setPaymentId(Long v){paymentId=v;} public BigDecimal getRequestedAmount(){return requestedAmount;} public void setRequestedAmount(BigDecimal v){requestedAmount=v;}
 public BigDecimal getApprovedAmount(){return approvedAmount;} public void setApprovedAmount(BigDecimal v){approvedAmount=v;} public BigDecimal getFeeAmount(){return feeAmount;} public void setFeeAmount(BigDecimal v){feeAmount=v;}
 public String getReason(){return reason;} public void setReason(String v){reason=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getReviewNote(){return reviewNote;} public void setReviewNote(String v){reviewNote=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getResolvedAt(){return resolvedAt;} public void setResolvedAt(LocalDateTime v){resolvedAt=v;}
}
