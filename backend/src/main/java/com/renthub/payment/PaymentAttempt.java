package com.renthub.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="RH_PAYMENT_ATTEMPTS")
public class PaymentAttempt {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="payment_attempt_seq") @SequenceGenerator(name="payment_attempt_seq",sequenceName="RH_PAYMENT_ATTEMPT_SEQ",allocationSize=1) private Long id;
 private Long bookingId; private Long userId; private BigDecimal amount; private String method; private String provider; private String providerReference; private String status; private String failureCode; private String failureMessage; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getBookingId(){return bookingId;} public void setBookingId(Long v){bookingId=v;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
 public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public String getMethod(){return method;} public void setMethod(String v){method=v;} public String getProvider(){return provider;} public void setProvider(String v){provider=v;}
 public String getProviderReference(){return providerReference;} public void setProviderReference(String v){providerReference=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getFailureCode(){return failureCode;} public void setFailureCode(String v){failureCode=v;} public String getFailureMessage(){return failureMessage;} public void setFailureMessage(String v){failureMessage=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
