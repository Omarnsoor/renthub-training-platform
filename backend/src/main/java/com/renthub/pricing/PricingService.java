package com.renthub.pricing;

import org.springframework.stereotype.Service;
import java.math.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class PricingService {
 private final PricingRuleRepository repo;
 public PricingService(PricingRuleRepository repo){this.repo=repo;}
 public record Adjustment(Long ruleId,String name,String ruleType,BigDecimal amount){}
 public record PriceResult(BigDecimal baseAmount,List<Adjustment> adjustments,BigDecimal finalAmount){}

 public PriceResult price(String assetType,String city,LocalDate date,BigDecimal base){
  BigDecimal total=base;List<Adjustment> adjustments=new ArrayList<>();
  for(PricingRule rule:repo.findByStatusOrderByPriorityAsc("ACTIVE")){
   if(rule.getAssetType()!=null&&!rule.getAssetType().equalsIgnoreCase(assetType))continue;
   if(rule.getCity()!=null&&!rule.getCity().equalsIgnoreCase(city))continue;
   if(rule.getStartDate()!=null&&date.isBefore(rule.getStartDate()))continue;
   if(rule.getEndDate()!=null&&date.isAfter(rule.getEndDate()))continue;
   BigDecimal delta=switch(rule.getRuleType()){
    case "PERCENT_UP"->total.multiply(rule.getValue()).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
    case "PERCENT_DOWN"->total.multiply(rule.getValue()).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP).negate();
    case "FIXED_UP"->rule.getValue();
    case "FIXED_DOWN"->rule.getValue().negate();
    default->BigDecimal.ZERO;
   };
   total=total.add(delta).max(BigDecimal.ZERO);adjustments.add(new Adjustment(rule.getId(),rule.getName(),rule.getRuleType(),delta));
  }
  return new PriceResult(base,adjustments,total.setScale(2,RoundingMode.HALF_UP));
 }
}
