package com.renthub.pricing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="RH_PRICING_RULES")
public class PricingRule {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="pricing_rule_seq") @SequenceGenerator(name="pricing_rule_seq",sequenceName="RH_PRICING_RULE_SEQ",allocationSize=1) private Long id;
 private String name; private String assetType; private String city; private String ruleType; private BigDecimal value; private Integer priority=100; private LocalDate startDate; private LocalDate endDate; private String status="ACTIVE";
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;} public String getAssetType(){return assetType;} public void setAssetType(String v){assetType=v;} public String getCity(){return city;} public void setCity(String v){city=v;}
 public String getRuleType(){return ruleType;} public void setRuleType(String v){ruleType=v;} public BigDecimal getValue(){return value;} public void setValue(BigDecimal v){value=v;} public Integer getPriority(){return priority;} public void setPriority(Integer v){priority=v;}
 public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;} public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
