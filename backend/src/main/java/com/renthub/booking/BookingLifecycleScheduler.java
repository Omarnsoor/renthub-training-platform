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
 private final BookingRepository bookings; private final BookingHistoryRepository history; private final SystemConfigRepository config; private final AuditService audit; private final NotificationService notifications;
 public BookingLifecycleScheduler(BookingRepository bookings,BookingHistoryRepository history,SystemConfigRepository config,AuditService audit,NotificationService notifications){this.bookings=bookings;this.history=history;this.config=config;this.audit=audit;this.notifications=notifications;}

 @Scheduled(fixedDelay=3600000)
 @Transactional
 public void expirePending(){
  int hours=config.findById("booking.pending.expiry.hours").map(c->Integer.parseInt(c.getConfigValue())).orElse(24);LocalDateTime cutoff=LocalDateTime.now().minusHours(hours);
  for(Booking b:bookings.findByStatusAndCreatedAtBefore("PENDING",cutoff)){String old=b.getStatus();b.setStatus("EXPIRED");bookings.save(b);BookingHistory h=new BookingHistory();h.setBookingId(b.getId());h.setOldStatus(old);h.setNewStatus("EXPIRED");h.setReason("Pending payment timeout");history.save(h);audit.record(null,"BOOKING_EXPIRED","BOOKING",b.getId(),old,"EXPIRED");notifications.send(b.getUserId(),"BOOKING","Booking expired","Booking #"+b.getId()+" expired because it was not paid in time","BOOKING",b.getId());}
 }
}
