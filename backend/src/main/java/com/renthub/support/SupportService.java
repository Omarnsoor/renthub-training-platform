package com.renthub.support;

import com.renthub.audit.AuditService;
import com.renthub.booking.BookingRepository;
import com.renthub.notification.NotificationService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SupportService {
 private final SupportTicketRepository tickets; private final TicketMessageRepository messages; private final BookingRepository bookings; private final AuditService audit; private final NotificationService notifications;
 public SupportService(SupportTicketRepository tickets,TicketMessageRepository messages,BookingRepository bookings,AuditService audit,NotificationService notifications){this.tickets=tickets;this.messages=messages;this.bookings=bookings;this.audit=audit;this.notifications=notifications;}

 @Transactional
 public SupportTicket open(User user,Map<String,Object> body){
  Long bookingId=body.get("bookingId")==null?null:Long.valueOf(String.valueOf(body.get("bookingId")));
  if(bookingId!=null){var b=bookings.findById(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));if(!b.getUserId().equals(user.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");}
  SupportTicket t=new SupportTicket();t.setUserId(user.getId());t.setBookingId(bookingId);t.setCategory(String.valueOf(body.getOrDefault("category","GENERAL")).toUpperCase());t.setSubject(required(body,"subject"));t.setDescription(required(body,"description"));t.setPriority(String.valueOf(body.getOrDefault("priority","NORMAL")).toUpperCase());t=tickets.save(t);
  audit.record(user.getId(),"TICKET_OPENED","SUPPORT_TICKET",t.getId(),null,t.getSubject());notifications.send(user.getId(),"SUPPORT","Support ticket opened","Ticket #"+t.getId()+" was created","SUPPORT_TICKET",t.getId());return t;
 }

 public List<TicketMessage> messages(User user,Long ticketId){SupportTicket t=ticket(user,ticketId);List<TicketMessage> all=messages.findByTicketIdOrderByCreatedAtAsc(ticketId);return "ADMIN".equalsIgnoreCase(user.getRole())?all:all.stream().filter(m->m.getInternalNote()==null||m.getInternalNote()==0).toList();}

 @Transactional
 public TicketMessage message(User user,Long ticketId,String text,boolean internal){SupportTicket t=ticket(user,ticketId);if(internal&&!"ADMIN".equalsIgnoreCase(user.getRole()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Internal notes are admin-only");if(text==null||text.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Message is required");TicketMessage m=new TicketMessage();m.setTicketId(ticketId);m.setSenderUserId(user.getId());m.setMessage(text.trim());m.setInternalNote(internal?1:0);m=messages.save(m);t.setUpdatedAt(LocalDateTime.now());if(!"ADMIN".equalsIgnoreCase(user.getRole()))t.setStatus("OPEN");tickets.save(t);if(!internal&&"ADMIN".equalsIgnoreCase(user.getRole()))notifications.send(t.getUserId(),"SUPPORT","New support reply","There is a new reply on ticket #"+ticketId,"SUPPORT_TICKET",ticketId);return m;}

 @Transactional
 public SupportTicket adminUpdate(Long adminId,Long id,Map<String,Object> body){SupportTicket t=tickets.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Ticket not found"));String old=t.getStatus();if(body.get("status")!=null)t.setStatus(String.valueOf(body.get("status")).toUpperCase());if(body.get("priority")!=null)t.setPriority(String.valueOf(body.get("priority")).toUpperCase());if(body.get("assignedTo")!=null)t.setAssignedTo(Long.valueOf(String.valueOf(body.get("assignedTo"))));if(body.get("resolution")!=null)t.setResolution(String.valueOf(body.get("resolution")));t.setUpdatedAt(LocalDateTime.now());tickets.save(t);audit.record(adminId,"TICKET_UPDATED","SUPPORT_TICKET",id,old,t.getStatus());if(!Objects.equals(old,t.getStatus()))notifications.send(t.getUserId(),"SUPPORT","Ticket status changed","Ticket #"+id+" is now "+t.getStatus(),"SUPPORT_TICKET",id);return t;}

 private SupportTicket ticket(User user,Long id){SupportTicket t=tickets.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Ticket not found"));if(!"ADMIN".equalsIgnoreCase(user.getRole())&&!t.getUserId().equals(user.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your ticket");return t;}
 private String required(Map<String,Object>b,String key){String v=b.get(key)==null?"":String.valueOf(b.get(key)).trim();if(v.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,key+" is required");return v;}
}
