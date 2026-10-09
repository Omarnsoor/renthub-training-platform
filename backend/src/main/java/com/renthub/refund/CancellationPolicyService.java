package com.renthub.refund;

import com.renthub.booking.Booking;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CancellationPolicyService {
  private final CancellationPolicyRepository policies;
  public CancellationPolicyService(CancellationPolicyRepository policies){this.policies=policies;}

  public record Decision(String policyCode,long hoursBeforeStart,BigDecimal feePercent,BigDecimal feeAmount,BigDecimal refundableAmount){}

  public Decision quote(Booking booking, BigDecimal paidAmount){
    long hours=Duration.between(LocalDateTime.now(),booking.getStartDate().atStartOfDay()).toHours();
    CancellationPolicy policy=policies.findByStatusOrderByPriorityAsc("ACTIVE").stream()
        .filter(p->"BOTH".equalsIgnoreCase(p.getAssetType())||booking.getAssetType().equalsIgnoreCase(p.getAssetType()))
        .filter(p->hours>=p.getMinHoursBeforeStart())
        .findFirst()
        .orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"No cancellation policy matches this booking"));
    BigDecimal pct=policy.getFeePercent().divide(BigDecimal.valueOf(100),6,RoundingMode.HALF_UP);
    BigDecimal fee=paidAmount.multiply(pct).setScale(2,RoundingMode.HALF_UP);
    BigDecimal refundable=paidAmount.subtract(fee).max(BigDecimal.ZERO).setScale(2,RoundingMode.HALF_UP);
    return new Decision(policy.getPolicyCode(),hours,policy.getFeePercent(),fee,refundable);
  }
}
