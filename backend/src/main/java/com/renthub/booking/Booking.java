package com.renthub.booking;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="RH_BOOKINGS")
public class Booking {
  @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="booking_seq")
  @SequenceGenerator(name="booking_seq",sequenceName="RH_BOOKING_SEQ",allocationSize=1)
  private Long id;
  @Column(nullable=false) private Long userId;
  @Column(nullable=false,length=20) private String assetType;
  @Column(nullable=false) private Long assetId;
  @Column(nullable=false) private LocalDate startDate;
  @Column(nullable=false) private LocalDate endDate;
  @Column(nullable=false,precision=12,scale=2) private BigDecimal totalAmount;
  @Column(nullable=false,length=30) private String status="PENDING";
  @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
  private String couponCode;
  private BigDecimal discountAmount=BigDecimal.ZERO;
  private BigDecimal cancellationFee=BigDecimal.ZERO;
  private BigDecimal depositAmount=BigDecimal.ZERO;
  private String checkinStatus="NOT_STARTED";
  private String ownerPayoutStatus="NOT_READY";

  public Long getId(){return id;} public void setId(Long v){id=v;}
  public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
  public String getAssetType(){return assetType;} public void setAssetType(String v){assetType=v;}
  public Long getAssetId(){return assetId;} public void setAssetId(Long v){assetId=v;}
  public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;}
  public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
  public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
  public String getCouponCode(){return couponCode;} public void setCouponCode(String v){couponCode=v;}
  public BigDecimal getDiscountAmount(){return discountAmount;} public void setDiscountAmount(BigDecimal v){discountAmount=v;}
  public BigDecimal getCancellationFee(){return cancellationFee;} public void setCancellationFee(BigDecimal v){cancellationFee=v;}
  public BigDecimal getDepositAmount(){return depositAmount;} public void setDepositAmount(BigDecimal v){depositAmount=v;}
  public String getCheckinStatus(){return checkinStatus;} public void setCheckinStatus(String v){checkinStatus=v;}
  public String getOwnerPayoutStatus(){return ownerPayoutStatus;} public void setOwnerPayoutStatus(String v){ownerPayoutStatus=v;}
}
