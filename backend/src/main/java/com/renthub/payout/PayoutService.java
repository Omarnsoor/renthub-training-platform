package com.renthub.payout;

import com.renthub.audit.AuditService;
import com.renthub.booking.Booking;
import com.renthub.car.CarRepository;
import com.renthub.notification.NotificationService;
import com.renthub.payment.PaymentRepository;
import com.renthub.property.PropertyRepository;
import com.renthub.settings.SystemConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.LocalDateTime;

@Service
public class PayoutService {
 private final OwnerPayoutRepository payouts; private final PaymentRepository payments; private final CarRepository cars; private final PropertyRepository props; private final SystemConfigRepository config; private final AuditService audit; private final NotificationService notifications; private final PayoutGuardService guard;
 public PayoutService(OwnerPayoutRepository payouts,PaymentRepository payments,CarRepository cars,PropertyRepository props,SystemConfigRepository config,AuditService audit,NotificationService notifications,PayoutGuardService guard){this.payouts=payouts;this.payments=payments;this.cars=cars;this.props=props;this.config=config;this.audit=audit;this.notifications=notifications;this.guard=guard;}

 @Transactional
 public OwnerPayout createForCompleted(Booking booking){
  return payouts.findByBookingId(booking.getId()).orElseGet(()->{
   var payment=payments.findByBookingId(booking.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"Paid booking has no payment record"));
   Long owner=owner(booking);BigDecimal pct=config.findById("platform.fee.percent").map(c->new BigDecimal(c.getConfigValue())).orElse(new BigDecimal("12.5"));
   BigDecimal fee=payment.getAmount().multiply(pct).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);OwnerPayout p=new OwnerPayout();p.setOwnerId(owner);p.setBookingId(booking.getId());p.setGrossAmount(payment.getAmount());p.setPlatformFee(fee);p.setPayoutAmount(payment.getAmount().subtract(fee));p.setStatus(guard.hasFinancialHold(booking.getId())?"ON_HOLD":"READY");p=payouts.save(p);
   audit.record(null,"PAYOUT_CREATED","OWNER_PAYOUT",p.getId(),null,"booking="+booking.getId()+", status="+p.getStatus());notifications.send(owner,"PAYOUT","Payout "+("READY".equals(p.getStatus())?"ready":"on hold"),"Payout "+p.getPayoutAmount()+" for booking #"+booking.getId()+" is "+p.getStatus().toLowerCase(),"OWNER_PAYOUT",p.getId());return p;
  });
 }

 @Transactional
 public OwnerPayout markPaid(Long adminId,Long id){OwnerPayout p=payouts.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Payout not found"));if("PAID".equals(p.getStatus()))return p;guard.assertPayable(p);p.setStatus("PAID");p.setPaidAt(LocalDateTime.now());payouts.save(p);audit.record(adminId,"PAYOUT_PAID","OWNER_PAYOUT",id,"READY","PAID");notifications.send(p.getOwnerId(),"PAYOUT","Payout sent","Payout #"+id+" has been marked paid","OWNER_PAYOUT",id);return p;}
 private Long owner(Booking b){if("CAR".equalsIgnoreCase(b.getAssetType()))return cars.findById(b.getAssetId()).map(c->c.getOwnerId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Car not found"));return props.findById(b.getAssetId()).map(p->p.getOwnerId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Property not found"));}
}
