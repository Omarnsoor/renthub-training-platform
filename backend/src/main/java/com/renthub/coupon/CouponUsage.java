package com.renthub.coupon;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="RH_COUPON_USAGES")
public class CouponUsage {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="coupon_usage_seq") @SequenceGenerator(name="coupon_usage_seq",sequenceName="RH_COUPON_USAGE_SEQ",allocationSize=1) private Long id;
 private Long couponId; private Long userId; private Long bookingId; private BigDecimal discountAmount; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getCouponId(){return couponId;} public void setCouponId(Long v){couponId=v;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
 public Long getBookingId(){return bookingId;} public void setBookingId(Long v){bookingId=v;} public BigDecimal getDiscountAmount(){return discountAmount;} public void setDiscountAmount(BigDecimal v){discountAmount=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
