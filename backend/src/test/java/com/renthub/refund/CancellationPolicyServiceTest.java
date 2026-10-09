package com.renthub.refund;

import com.renthub.booking.Booking;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CancellationPolicyServiceTest {
  @Test
  void selectsFirstMatchingPolicyByPriorityAndCalculatesFee() {
    CancellationPolicyRepository repo=mock(CancellationPolicyRepository.class);
    CancellationPolicy sevenDays=policy("FLEX_7D",168,"0",10);
    CancellationPolicy fortyEight=policy("STANDARD_48H",48,"10",20);
    CancellationPolicy late=policy("LATE_BEFORE_START",1,"30",30);
    when(repo.findByStatusOrderByPriorityAsc("ACTIVE")).thenReturn(List.of(sevenDays,fortyEight,late));
    Booking booking=new Booking();booking.setAssetType("CAR");booking.setStartDate(LocalDate.now().plusDays(4));
    CancellationPolicyService.Decision result=new CancellationPolicyService(repo).quote(booking,new BigDecimal("100.00"));
    assertEquals("STANDARD_48H",result.policyCode());
    assertEquals(new BigDecimal("10.00"),result.feeAmount());
    assertEquals(new BigDecimal("90.00"),result.refundableAmount());
  }

  private CancellationPolicy policy(String code,long hours,String percent,int priority){
    CancellationPolicy p=new CancellationPolicy();p.setPolicyCode(code);p.setAssetType("BOTH");p.setMinHoursBeforeStart(hours);p.setFeePercent(new BigDecimal(percent));p.setPriority(priority);p.setStatus("ACTIVE");return p;
  }
}
