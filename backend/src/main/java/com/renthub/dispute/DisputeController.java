package com.renthub.dispute;

import com.renthub.audit.AuditService;
import com.renthub.auth.AuthService;
import com.renthub.booking.BookingRepository;
import com.renthub.notification.NotificationService;
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
 private final DisputeRepository repo; private final BookingRepository bookings; private final AuthService auth; private final AuditService audit; private final NotificationService notifications;
 public DisputeController(DisputeRepository repo,BookingRepository bookings,AuthService auth,AuditService audit,NotificationService notifications){this.repo=repo;this.bookings=bookings;this.auth=auth;this.audit=audit;this.notifications=notifications;}
 @GetMapping("/mine") public List<Dispute> mine(@RequestHeader(value="Authorization",required=false)String h){return repo.findByOpenedByOrderByCreatedAtDesc(auth.require(h).getId());}
 @PostMapping public Dispute open(@RequestHeader(value="Authorization",required=false)String h,@RequestBody Map<String,Object>b){User u=auth.require(h);Long bookingId=Long.valueOf(String.valueOf(b.get("bookingId")));var booking=bookings.findById(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));if(!booking.getUserId().equals(u.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");Dispute d=new Dispute();d.setBookingId(bookingId);d.setOpenedBy(u.getId());d.setCategory(String.valueOf(b.getOrDefault("category","OTHER")).toUpperCase());d.setDescription(String.valueOf(b.getOrDefault("description","")));if(d.getDescription().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Description is required");d=repo.save(d);audit.record(u.getId(),"DISPUTE_OPENED","DISPUTE",d.getId(),null,"booking="+bookingId);notifications.send(u.getId(),"DISPUTE","Dispute opened","Dispute #"+d.getId()+" is under review","DISPUTE",d.getId());return d;}
 @GetMapping("/admin") public List<Dispute> admin(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"ADMIN");return repo.findAll();}
 @PatchMapping("/{id}/admin") public Dispute resolve(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@RequestBody Map<String,Object>b){User u=auth.require(h);auth.requireRole(u,"ADMIN");Dispute d=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Dispute not found"));String old=d.getStatus();d.setStatus(String.valueOf(b.getOrDefault("status","UNDER_REVIEW")).toUpperCase());if(b.get("resolution")!=null)d.setResolution(String.valueOf(b.get("resolution")));if(List.of("RESOLVED","REJECTED").contains(d.getStatus()))d.setResolvedAt(LocalDateTime.now());repo.save(d);audit.record(u.getId(),"DISPUTE_UPDATED","DISPUTE",id,old,d.getStatus());notifications.send(d.getOpenedBy(),"DISPUTE","Dispute status changed","Dispute #"+id+" is now "+d.getStatus(),"DISPUTE",id);return d;}
}
