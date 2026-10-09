package com.renthub.dispute;

import com.renthub.audit.AuditService;
import com.renthub.auth.AuthService;
import com.renthub.booking.BookingRepository;
import com.renthub.notification.NotificationService;
import com.renthub.payout.PayoutGuardService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/disputes")
@CrossOrigin(origins="http://localhost:5173")
public class DisputeController {
 private static final Set<String> DISPUTABLE_BOOKING_STATES=Set.of("PAID","COMPLETED","REFUND_PENDING","REFUND_APPROVED","REFUNDED");
 private final DisputeRepository repo; private final BookingRepository bookings; private final AuthService auth; private final AuditService audit; private final NotificationService notifications; private final PayoutGuardService payouts;
 public DisputeController(DisputeRepository repo,BookingRepository bookings,AuthService auth,AuditService audit,NotificationService notifications,PayoutGuardService payouts){this.repo=repo;this.bookings=bookings;this.auth=auth;this.audit=audit;this.notifications=notifications;this.payouts=payouts;}
 @GetMapping("/mine") public List<Dispute> mine(@RequestHeader(value="Authorization",required=false)String h){return repo.findByOpenedByOrderByCreatedAtDesc(auth.require(h).getId());}
 @PostMapping public Dispute open(@RequestHeader(value="Authorization",required=false)String h,@RequestBody Map<String,Object>b){User u=auth.require(h);Long bookingId=Long.valueOf(String.valueOf(b.get("bookingId")));var booking=bookings.findById(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));if(!booking.getUserId().equals(u.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");if(!DISPUTABLE_BOOKING_STATES.contains(String.valueOf(booking.getStatus()).toUpperCase()))throw new ResponseStatusException(HttpStatus.CONFLICT,"A dispute can only be opened after payment");boolean active=repo.findByBookingIdOrderByCreatedAtDesc(bookingId).stream().anyMatch(x->List.of("OPEN","UNDER_REVIEW").contains(String.valueOf(x.getStatus()).toUpperCase()));if(active)throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking already has an active dispute");Dispute d=new Dispute();d.setBookingId(bookingId);d.setOpenedBy(u.getId());d.setCategory(String.valueOf(b.getOrDefault("category","OTHER")).toUpperCase());d.setDescription(String.valueOf(b.getOrDefault("description","")));if(d.getDescription().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Description is required");d=repo.save(d);payouts.hold(bookingId,u.getId(),"Dispute #"+d.getId()+" opened");audit.record(u.getId(),"DISPUTE_OPENED","DISPUTE",d.getId(),null,"booking="+bookingId);notifications.send(u.getId(),"DISPUTE","Dispute opened","Dispute #"+d.getId()+" is under review","DISPUTE",d.getId());return d;}
 @GetMapping("/admin") public List<Dispute> admin(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"ADMIN");return repo.findAll();}
 @PatchMapping("/{id}/admin") public Dispute resolve(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@RequestBody Map<String,Object>b){User u=auth.require(h);auth.requireRole(u,"ADMIN");Dispute d=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Dispute not found"));String old=d.getStatus();String next=String.valueOf(b.getOrDefault("status","UNDER_REVIEW")).toUpperCase();if(!List.of("OPEN","UNDER_REVIEW","RESOLVED","REJECTED").contains(next))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported dispute status");d.setStatus(next);if(b.get("resolution")!=null)d.setResolution(String.valueOf(b.get("resolution")));if(List.of("RESOLVED","REJECTED").contains(d.getStatus()))d.setResolvedAt(LocalDateTime.now());repo.save(d);if(List.of("RESOLVED","REJECTED").contains(d.getStatus()))payouts.releaseIfClear(d.getBookingId(),u.getId(),"Dispute #"+id+" closed");audit.record(u.getId(),"DISPUTE_UPDATED","DISPUTE",id,old,d.getStatus());notifications.send(d.getOpenedBy(),"DISPUTE","Dispute status changed","Dispute #"+id+" is now "+d.getStatus(),"DISPUTE",id);return d;}
}
