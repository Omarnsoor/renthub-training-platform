package com.renthub.refund;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="RH_CANCELLATION_POLICIES")
public class CancellationPolicy {
  @Id
  @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="cancel_policy_seq")
  @SequenceGenerator(name="cancel_policy_seq",sequenceName="RH_CANCEL_POLICY_SEQ",allocationSize=1)
  private Long id;
  @Column(name="POLICY_CODE",nullable=false,length=40) private String policyCode;
  @Column(name="ASSET_TYPE",nullable=false,length=20) private String assetType="BOTH";
  @Column(name="MIN_HOURS_BEFORE_START",nullable=false) private Long minHoursBeforeStart;
  @Column(name="FEE_PERCENT",nullable=false,precision=5,scale=2) private BigDecimal feePercent;
  @Column(nullable=false) private Integer priority=100;
  @Column(nullable=false,length=20) private String status="ACTIVE";
  @Column(length=300) private String description;
  public Long getId(){return id;} public void setId(Long v){id=v;}
  public String getPolicyCode(){return policyCode;} public void setPolicyCode(String v){policyCode=v;}
  public String getAssetType(){return assetType;} public void setAssetType(String v){assetType=v;}
  public Long getMinHoursBeforeStart(){return minHoursBeforeStart;} public void setMinHoursBeforeStart(Long v){minHoursBeforeStart=v;}
  public BigDecimal getFeePercent(){return feePercent;} public void setFeePercent(BigDecimal v){feePercent=v;}
  public Integer getPriority(){return priority;} public void setPriority(Integer v){priority=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public String getDescription(){return description;} public void setDescription(String v){description=v;}
}
