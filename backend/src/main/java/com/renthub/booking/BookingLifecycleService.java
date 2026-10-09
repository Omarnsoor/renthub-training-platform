package com.renthub.booking;

import com.renthub.audit.AuditService;
import com.renthub.notification.NotificationService;
import com.renthub.payout.PayoutService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service
public class BookingLifecycleService {
 private final BookingRepository bookings; private final BookingHistoryRepository history; private final AuditService audit; private final NotificationService notifications; private final PayoutService payouts;
 public BookingLifecycleService(BookingRepository bookings,BookingHistoryRepository history,AuditService audit,NotificationService notifications,PayoutService payouts){this.bookings=bookings;this.history=history;this.audit=audit;this.notifications=notifications;this.payouts=payouts;}

 @Transactional
 public Booking checkIn(User user,Long id){
  Booking b=owned(user,id);if(!"PAID".equalsIgnoreCase(b.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only paid bookings can check in");
  LocalDate today=LocalDate.now();if(today.isBefore(b.getStartDate()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Check-in is not open yet");if(!today.isBefore(b.getEndDate()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking has already ended");
  b.setCheckinStatus("CHECKED_IN");saveTransition(b,user.getId(),b.getStatus(),"PAID","Customer check-in");audit.record(user.getId(),"BOOKING_CHECKED_IN","BOOKING",id,"NOT_STARTED","CHECKED_IN");notifications.send(user.getId(),"BOOKING","Checked in","Booking #"+id+" is checked in","BOOKING",id);return b;
 }

 @Transactional
 public Booking complete(User actor,Long id,boolean force){
  Booking b=bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));
  if(!"ADMIN".equalsIgnoreCase(actor.getRole()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Admin role required");
  if(!"PAID".equalsIgnoreCase(b.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only paid bookings can complete");
  if(!force&&LocalDate.now().isBefore(b.getEndDate()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking has not reached its end date");
  String old=b.getStatus();b.setStatus("COMPLETED");b.setCheckinStatus("COMPLETED");b.setOwnerPayoutStatus("READY");bookings.save(b);saveTransition(b,actor.getId(),old,"COMPLETED",force?"Admin forced completion":"End date reached");payouts.createForCompleted(b);audit.record(actor.getId(),"BOOKING_COMPLETED","BOOKING",id,old,"COMPLETED");notifications.send(b.getUserId(),"BOOKING","Trip completed","Booking #"+id+" is complete. You can leave a review.","BOOKING",id);return b;
 }

 public List<BookingHistory> history(User user,Long id){Booking b=bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));if(!b.getUserId().equals(user.getId())&&!"ADMIN".equalsIgnoreCase(user.getRole()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");return history.findByBookingIdOrderByCreatedAtAsc(id);}

 private Booking owned(User u,Long id){Booking b=bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));if(!b.getUserId().equals(u.getId())&&!"ADMIN".equalsIgnoreCase(u.getRole()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");return b;}
 private void saveTransition(Booking b,Long actor,String oldStatus,String newStatus,String reason){BookingHistory h=new BookingHistory();h.setBookingId(b.getId());h.setOldStatus(oldStatus);h.setNewStatus(newStatus);h.setChangedBy(actor);h.setReason(reason);history.save(h);bookings.save(b);}
}
