package com.renthub.coupon;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="RH_COUPONS")
public class Coupon {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="coupon_seq") @SequenceGenerator(name="coupon_seq",sequenceName="RH_COUPON_SEQ",allocationSize=1) private Long id;
 private String code; private String description; private String discountType; private BigDecimal discountValue; private BigDecimal minAmount; private BigDecimal maxDiscount;
 private LocalDateTime startsAt; private LocalDateTime expiresAt; private Integer globalUsageLimit; private Integer perUserLimit; private Integer usedCount=0; private String status="ACTIVE";
 public Long getId(){return id;} public String getCode(){return code;} public void setCode(String v){code=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public String getDiscountType(){return discountType;} public void setDiscountType(String v){discountType=v;} public BigDecimal getDiscountValue(){return discountValue;} public void setDiscountValue(BigDecimal v){discountValue=v;}
 public BigDecimal getMinAmount(){return minAmount;} public void setMinAmount(BigDecimal v){minAmount=v;} public BigDecimal getMaxDiscount(){return maxDiscount;} public void setMaxDiscount(BigDecimal v){maxDiscount=v;}
 public LocalDateTime getStartsAt(){return startsAt;} public void setStartsAt(LocalDateTime v){startsAt=v;} public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime v){expiresAt=v;}
 public Integer getGlobalUsageLimit(){return globalUsageLimit;} public void setGlobalUsageLimit(Integer v){globalUsageLimit=v;} public Integer getPerUserLimit(){return perUserLimit;} public void setPerUserLimit(Integer v){perUserLimit=v;}
 public Integer getUsedCount(){return usedCount;} public void setUsedCount(Integer v){usedCount=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
