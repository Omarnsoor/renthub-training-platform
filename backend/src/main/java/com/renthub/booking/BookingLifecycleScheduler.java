package com.renthub.booking;

import com.renthub.audit.AuditService;
import com.renthub.notification.NotificationService;
import com.renthub.settings.SystemConfigRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class BookingLifecycleScheduler {
 private final BookingRepository bookings; private final SystemConfigRepository config; private final AuditService audit; private final NotificationService notifications; private final BookingTransitionService transitions;
 public BookingLifecycleScheduler(BookingRepository bookings,SystemConfigRepository config,AuditService audit,NotificationService notifications,BookingTransitionService transitions){this.bookings=bookings;this.config=config;this.audit=audit;this.notifications=notifications;this.transitions=transitions;}

 @Scheduled(fixedDelay=3600000)
 @Transactional
 public void expirePending(){
  int hours=config.findById("booking.pending.expiry.hours").map(c->Integer.parseInt(c.getConfigValue())).orElse(24);LocalDateTime cutoff=LocalDateTime.now().minusHours(hours);
  for(Booking b:bookings.findByStatusAndCreatedAtBefore("PENDING",cutoff)){String old=b.getStatus();transitions.transition(b,null,"EXPIRED","Pending payment timeout");audit.record(null,"BOOKING_EXPIRED","BOOKING",b.getId(),old,"EXPIRED");notifications.send(b.getUserId(),"BOOKING","Booking expired","Booking #"+b.getId()+" expired because it was not paid in time","BOOKING",b.getId());}
 }
}
