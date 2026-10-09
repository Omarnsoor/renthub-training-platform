package com.renthub.payout;

import com.renthub.audit.AuditService;
import com.renthub.dispute.DisputeRepository;
import com.renthub.refund.RefundRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PayoutGuardService {
  private static final List<String> ACTIVE_DISPUTES = List.of("OPEN","UNDER_REVIEW");
  private static final List<String> ACTIVE_REFUNDS = List.of("REQUESTED","APPROVED");

  private final OwnerPayoutRepository payouts;
  private final DisputeRepository disputes;
  private final RefundRepository refunds;
  private final AuditService audit;

  public PayoutGuardService(OwnerPayoutRepository payouts, DisputeRepository disputes, RefundRepository refunds, AuditService audit) {
    this.payouts = payouts;
    this.disputes = disputes;
    this.refunds = refunds;
    this.audit = audit;
  }

  public boolean hasFinancialHold(Long bookingId) {
    boolean disputeHold = disputes.findByBookingIdOrderByCreatedAtDesc(bookingId).stream()
        .anyMatch(d -> ACTIVE_DISPUTES.contains(String.valueOf(d.getStatus()).toUpperCase()));
    boolean refundHold = refunds.findByBookingIdOrderByCreatedAtDesc(bookingId).stream()
        .anyMatch(r -> ACTIVE_REFUNDS.contains(String.valueOf(r.getStatus()).toUpperCase()));
    return disputeHold || refundHold;
  }

  public void assertPayable(OwnerPayout payout) {
    if (hasFinancialHold(payout.getBookingId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Payout is blocked by an active dispute or refund");
    }
    if (!"READY".equalsIgnoreCase(payout.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Only READY payouts can be paid");
    }
  }

  public void hold(Long bookingId, Long actorId, String reason) {
    payouts.findByBookingId(bookingId).ifPresent(p -> {
      if (!"PAID".equalsIgnoreCase(p.getStatus()) && !"ON_HOLD".equalsIgnoreCase(p.getStatus())) {
        String old = p.getStatus();
        p.setStatus("ON_HOLD");
        payouts.save(p);
        audit.record(actorId,"PAYOUT_HELD","OWNER_PAYOUT",p.getId(),old,"ON_HOLD: "+reason);
      }
    });
  }

  public void releaseIfClear(Long bookingId, Long actorId, String reason) {
    if (hasFinancialHold(bookingId)) return;
    payouts.findByBookingId(bookingId).ifPresent(p -> {
      if ("ON_HOLD".equalsIgnoreCase(p.getStatus())) {
        p.setStatus("READY");
        payouts.save(p);
        audit.record(actorId,"PAYOUT_RELEASED","OWNER_PAYOUT",p.getId(),"ON_HOLD","READY: "+reason);
      }
    });
  }
}
