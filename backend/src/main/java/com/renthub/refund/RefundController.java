package com.renthub.refund;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin(origins="http://localhost:5173")
public class RefundController {
 private final RefundRepository repo; private final RefundService service; private final AuthService auth;
 public RefundController(RefundRepository repo,RefundService service,AuthService auth){this.repo=repo;this.service=service;this.auth=auth;}
 @GetMapping("/mine") public List<Refund> mine(@RequestHeader(value="Authorization",required=false)String h){return repo.findByUserIdOrderByCreatedAtDesc(auth.require(h).getId());}
 @GetMapping("/booking/{bookingId}/quote") public CancellationPolicyService.Decision quote(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long bookingId){User u=auth.require(h);return service.quote(u.getId(),bookingId);}
 @PostMapping("/booking/{bookingId}") public Refund request(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long bookingId,@RequestBody(required=false)Map<String,Object>b){User u=auth.require(h);return service.request(u.getId(),bookingId,b==null?null:String.valueOf(b.getOrDefault("reason","Customer request")));}
 @GetMapping("/admin") public List<Refund> admin(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"ADMIN");return repo.findAll();}
 @PatchMapping("/{id}/decision") public Refund decision(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@RequestBody Map<String,Object>b){User u=auth.require(h);auth.requireRole(u,"ADMIN");boolean approve=Boolean.parseBoolean(String.valueOf(b.getOrDefault("approve","false")));BigDecimal amount=b.get("approvedAmount")==null?null:new BigDecimal(String.valueOf(b.get("approvedAmount")));return service.decide(u.getId(),id,approve,amount,b.get("note")==null?null:String.valueOf(b.get("note")));}
 @PatchMapping("/{id}/paid") public Refund paid(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){User u=auth.require(h);auth.requireRole(u,"ADMIN");return service.markPaid(u.getId(),id);}
}
