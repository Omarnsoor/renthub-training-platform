package com.renthub.pricing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="RH_BOOKING_PRICE_ADJ")
public class BookingPriceAdjustment {
  @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="price_adj_seq")
  @SequenceGenerator(name="price_adj_seq",sequenceName="RH_PRICE_ADJ_SEQ",allocationSize=1)
  private Long id;
  private Long bookingId;
  private Long ruleId;
  private String ruleName;
  private String ruleType;
  private BigDecimal amount;
  private LocalDateTime createdAt=LocalDateTime.now();
  public Long getId(){return id;} public Long getBookingId(){return bookingId;} public void setBookingId(Long v){bookingId=v;} public Long getRuleId(){return ruleId;} public void setRuleId(Long v){ruleId=v;}
  public String getRuleName(){return ruleName;} public void setRuleName(String v){ruleName=v;} public String getRuleType(){return ruleType;} public void setRuleType(String v){ruleType=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
