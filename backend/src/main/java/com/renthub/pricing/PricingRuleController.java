package com.renthub.pricing;

import com.renthub.audit.AuditService;
import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing-rules")
@CrossOrigin(origins="http://localhost:5173")
public class PricingRuleController {
 private final PricingRuleRepository repo; private final AuthService auth; private final AuditService audit;
 public PricingRuleController(PricingRuleRepository repo,AuthService auth,AuditService audit){this.repo=repo;this.auth=auth;this.audit=audit;}
 @GetMapping public List<PricingRule> all(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"ADMIN");return repo.findAll();}
 @PostMapping public PricingRule create(@RequestHeader(value="Authorization",required=false)String h,@RequestBody PricingRule r){User u=auth.require(h);auth.requireRole(u,"ADMIN");PricingRule saved=repo.save(r);audit.record(u.getId(),"PRICING_RULE_CREATED","PRICING_RULE",saved.getId(),null,saved.getName());return saved;}
 @PatchMapping("/{id}/status/{status}") public PricingRule status(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@PathVariable String status){User u=auth.require(h);auth.requireRole(u,"ADMIN");PricingRule r=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Pricing rule not found"));String old=r.getStatus();r.setStatus(status.toUpperCase());repo.save(r);audit.record(u.getId(),"PRICING_RULE_STATUS","PRICING_RULE",id,old,r.getStatus());return r;}
}
