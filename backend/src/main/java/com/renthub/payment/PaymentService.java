package com.renthub.payment;

import com.renthub.audit.AuditService;
import com.renthub.booking.*;
import com.renthub.bookingservice.*;
import com.renthub.integration.payment.PaymentGateway;
import com.renthub.notification.NotificationService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class PaymentService {
 private final PaymentRepository payments; private final PaymentAttemptRepository attempts; private final BookingRepository bookings; private final BookingServiceAddonRepository addons; private final PaymentGateway gateway; private final AuditService audit; private final NotificationService notifications; private final BookingTransitionService transitions;
 public PaymentService(PaymentRepository payments,PaymentAttemptRepository attempts,BookingRepository bookings,BookingServiceAddonRepository addons,PaymentGateway gateway,AuditService audit,NotificationService notifications,BookingTransitionService transitions){this.payments=payments;this.attempts=attempts;this.bookings=bookings;this.addons=addons;this.gateway=gateway;this.audit=audit;this.notifications=notifications;this.transitions=transitions;}

 public Payment receipt(User user,Long bookingId){Booking b=booking(bookingId);authorize(user,b);return payments.findByBookingId(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment not found"));}
 public List<PaymentAttempt> attempts(User user,Long bookingId){Booking b=booking(bookingId);authorize(user,b);return attempts.findByBookingIdOrderByCreatedAtDesc(bookingId);}

 @Transactional
 public Payment pay(User user,Long bookingId,String method,String scenario){
  Booking b=booking(bookingId);if(!b.getUserId().equals(user.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");
  if(List.of("CANCELLED","REFUND_PENDING","REFUND_APPROVED","REFUNDED","COMPLETED").contains(b.getStatus().toUpperCase()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking cannot be paid in its current state");
  if("PAID".equalsIgnoreCase(b.getStatus()))return payments.findByBookingId(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"Booking is already paid"));
  BigDecimal extras=addons.findByBookingId(bookingId).stream().map(BookingServiceAddon::getPrice).reduce(BigDecimal.ZERO,BigDecimal::add);
  BigDecimal deposit=b.getDepositAmount()==null?BigDecimal.ZERO:b.getDepositAmount();BigDecimal total=b.getTotalAmount().add(extras).add(deposit);
  String payMethod=method==null?"CARD":method.toUpperCase();
  PaymentAttempt attempt=new PaymentAttempt();attempt.setBookingId(bookingId);attempt.setUserId(user.getId());attempt.setAmount(total);attempt.setMethod(payMethod);attempt.setProvider("LOCALPAY");attempt.setStatus("STARTED");attempt=attempts.save(attempt);
  PaymentGateway.GatewayResult result=gateway.charge(bookingId,total,payMethod,scenario);attempt.setProvider(result.provider());attempt.setProviderReference(result.reference());
  if(!result.success()){attempt.setStatus("FAILED");attempt.setFailureCode(result.failureCode());attempt.setFailureMessage(result.message());attempts.save(attempt);audit.record(user.getId(),"PAYMENT_FAILED","PAYMENT_ATTEMPT",attempt.getId(),null,result.failureCode());notifications.send(user.getId(),"PAYMENT","Payment failed",result.message(),"BOOKING",bookingId);throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,result.message());}
  attempt.setStatus("CAPTURED");attempts.save(attempt);
  Payment p=payments.findByBookingId(bookingId).orElseGet(Payment::new);p.setBookingId(bookingId);p.setUserId(user.getId());p.setAmount(total);p.setMethod(payMethod);p.setStatus("PAID");p.setReferenceNo(result.reference());p=payments.save(p);
  String old=b.getStatus();transitions.transition(b,user.getId(),"PAID","Payment captured: "+result.reference());audit.record(user.getId(),"PAYMENT_CAPTURED","PAYMENT",p.getId(),old,"PAID amount="+total);notifications.send(user.getId(),"PAYMENT","Payment successful","Booking #"+bookingId+" was paid successfully","BOOKING",bookingId);return p;
 }

 private Booking booking(Long id){return bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));}
 private void authorize(User u,Booking b){if(!b.getUserId().equals(u.getId())&&!"ADMIN".equalsIgnoreCase(u.getRole()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");}
}
