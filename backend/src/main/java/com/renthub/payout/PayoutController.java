package com.renthub.payout;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/payouts")
@CrossOrigin(origins="http://localhost:5173")
public class PayoutController {
 private final OwnerPayoutRepository repo; private final PayoutService service; private final AuthService auth;
 public PayoutController(OwnerPayoutRepository repo,PayoutService service,AuthService auth){this.repo=repo;this.service=service;this.auth=auth;}
 @GetMapping("/mine") public List<OwnerPayout> mine(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"OWNER","ADMIN");return "ADMIN".equalsIgnoreCase(u.getRole())?repo.findAll():repo.findByOwnerIdOrderByCreatedAtDesc(u.getId());}
 @GetMapping("/admin") public List<OwnerPayout> admin(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"ADMIN");return repo.findAll();}
 @PatchMapping("/{id}/paid") public OwnerPayout paid(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){User u=auth.require(h);auth.requireRole(u,"ADMIN");return service.markPaid(u.getId(),id);}
}
