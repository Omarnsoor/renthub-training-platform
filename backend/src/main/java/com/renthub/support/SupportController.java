package com.renthub.support;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/support/tickets")
@CrossOrigin(origins="http://localhost:5173")
public class SupportController {
 private final SupportTicketRepository tickets; private final SupportService service; private final AuthService auth;
 public SupportController(SupportTicketRepository tickets,SupportService service,AuthService auth){this.tickets=tickets;this.service=service;this.auth=auth;}
 @GetMapping("/mine") public List<SupportTicket> mine(@RequestHeader(value="Authorization",required=false)String h){return tickets.findByUserIdOrderByUpdatedAtDesc(auth.require(h).getId());}
 @PostMapping public SupportTicket open(@RequestHeader(value="Authorization",required=false)String h,@RequestBody Map<String,Object>b){return service.open(auth.require(h),b);}
 @GetMapping("/{id}/messages") public List<TicketMessage> messages(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){return service.messages(auth.require(h),id);}
 @PostMapping("/{id}/messages") public TicketMessage message(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@RequestBody Map<String,Object>b){User u=auth.require(h);return service.message(u,id,String.valueOf(b.getOrDefault("message","")),Boolean.parseBoolean(String.valueOf(b.getOrDefault("internal","false"))));}
 @GetMapping("/admin") public List<SupportTicket> admin(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"ADMIN");return tickets.findAll();}
 @PatchMapping("/{id}/admin") public SupportTicket adminUpdate(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@RequestBody Map<String,Object>b){User u=auth.require(h);auth.requireRole(u,"ADMIN");return service.adminUpdate(u.getId(),id,b);}
}
