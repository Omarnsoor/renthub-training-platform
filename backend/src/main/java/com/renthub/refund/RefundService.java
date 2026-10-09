package com.renthub.refund;

import com.renthub.audit.AuditService;
import com.renthub.booking.*;
import com.renthub.notification.NotificationService;
import com.renthub.payment.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.*;
import java.util.List;

@Service
public class RefundService {
 private final RefundRepository refunds; private final BookingRepository bookings; private final PaymentRepository payments; private final AuditService audit; private final NotificationService notifications; private final CancellationPolicyService cancellationPolicies; private final BookingTransitionService transitions;
 public RefundService(RefundRepository refunds,BookingRepository bookings,PaymentRepository payments,AuditService audit,NotificationService notifications,CancellationPolicyService cancellationPolicies,BookingTransitionService transitions){this.refunds=refunds;this.bookings=bookings;this.payments=payments;this.audit=audit;this.notifications=notifications;this.cancellationPolicies=cancellationPolicies;this.transitions=transitions;}

 @Transactional
 public Refund request(Long userId,Long bookingId,String reason){
  Booking b=bookings.findById(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));
  if(!b.getUserId().equals(userId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");
  if(!"PAID".equalsIgnoreCase(b.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only paid bookings can request a refund");
  if(refunds.existsByBookingIdAndStatusIn(bookingId,List.of("REQUESTED","APPROVED","PAID")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking already has an active refund");
  Payment p=payments.findByBookingId(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"Payment record not found"));
  CancellationPolicyService.Decision decision=cancellationPolicies.quote(b,p.getAmount());
  BigDecimal fee=decision.feeAmount();
  Refund r=new Refund();r.setBookingId(bookingId);r.setUserId(userId);r.setPaymentId(p.getId());r.setRequestedAmount(decision.refundableAmount());r.setFeeAmount(fee);r.setReason(reason);r=refunds.save(r);
  b.setCancellationFee(fee);b=transitions.transition(b,userId,"REFUND_PENDING","Refund requested under policy "+decision.policyCode());
  audit.record(userId,"REFUND_REQUESTED","REFUND",r.getId(),null,"booking="+bookingId+", policy="+decision.policyCode()+", fee="+fee);
  notifications.send(userId,"REFUND","Refund requested","Refund request #"+r.getId()+" is under review","REFUND",r.getId());
  return r;
 }

 @Transactional
 public Refund decide(Long adminId,Long refundId,boolean approve,BigDecimal approvedAmount,String note){
  Refund r=refunds.findById(refundId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Refund not found"));
  if(!"REQUESTED".equals(r.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Refund is not awaiting review");
  if(approve){BigDecimal max=r.getRequestedAmount();BigDecimal amount=approvedAmount==null?max:approvedAmount;if(amount.signum()<0||amount.compareTo(max)>0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Approved amount is outside the allowed range");r.setApprovedAmount(amount);r.setStatus("APPROVED");}
  else{r.setApprovedAmount(BigDecimal.ZERO);r.setStatus("REJECTED");}
  r.setReviewNote(note);r.setResolvedAt(LocalDateTime.now());r=refunds.save(r);
  Booking b=bookings.findById(r.getBookingId()).orElseThrow();transitions.transition(b,adminId,approve?"REFUND_APPROVED":"PAID",approve?"Refund approved":"Refund rejected");
  audit.record(adminId,approve?"REFUND_APPROVED":"REFUND_REJECTED","REFUND",r.getId(),"REQUESTED",r.getStatus());
  notifications.send(r.getUserId(),"REFUND",approve?"Refund approved":"Refund rejected",approve?"Approved amount: "+r.getApprovedAmount():(note==null?"Refund request was rejected":note),"REFUND",r.getId());
  return r;
 }

 @Transactional
 public Refund markPaid(Long adminId,Long refundId){
  Refund r=refunds.findById(refundId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Refund not found"));
  if(!"APPROVED".equals(r.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Refund must be approved first");
  r.setStatus("PAID");r.setResolvedAt(LocalDateTime.now());refunds.save(r);
  Booking b=bookings.findById(r.getBookingId()).orElseThrow();transitions.transition(b,adminId,"REFUNDED","Refund payment completed");
  audit.record(adminId,"REFUND_PAID","REFUND",r.getId(),"APPROVED","PAID");
  notifications.send(r.getUserId(),"REFUND","Refund completed","Your refund of "+r.getApprovedAmount()+" has been completed","REFUND",r.getId());
  return r;
 }
}
