package com.renthub.pricing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PricingSnapshotService {
  private final BookingPriceAdjustmentRepository repo;

  public PricingSnapshotService(BookingPriceAdjustmentRepository repo){this.repo=repo;}

  @Transactional
  public void capture(Long bookingId, PricingService.PriceResult result){
    for(PricingService.Adjustment a:result.adjustments()){
      BookingPriceAdjustment row=new BookingPriceAdjustment();
      row.setBookingId(bookingId);row.setRuleId(a.ruleId());row.setRuleName(a.name());row.setRuleType(a.ruleType());row.setAmount(a.amount());repo.save(row);
    }
  }

  public List<BookingPriceAdjustment> forBooking(Long bookingId){return repo.findByBookingIdOrderByIdAsc(bookingId);}
}
