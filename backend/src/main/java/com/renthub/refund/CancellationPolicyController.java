package com.renthub.refund;

import com.renthub.audit.AuditService;
import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin/cancellation-policies")
@CrossOrigin(origins="http://localhost:5173")
public class CancellationPolicyController {
  private final CancellationPolicyRepository repo;
  private final AuthService auth;
  private final AuditService audit;

  public CancellationPolicyController(CancellationPolicyRepository repo,AuthService auth,AuditService audit){this.repo=repo;this.auth=auth;this.audit=audit;}

  @GetMapping
  public List<CancellationPolicy> all(@RequestHeader(value="Authorization",required=false)String header){User u=auth.require(header);auth.requireRole(u,"ADMIN");return repo.findAll();}

  @PostMapping
  public CancellationPolicy create(@RequestHeader(value="Authorization",required=false)String header,@RequestBody CancellationPolicy input){User u=auth.require(header);auth.requireRole(u,"ADMIN");validate(input);input.setId(null);normalize(input);CancellationPolicy saved=repo.save(input);audit.record(u.getId(),"CANCELLATION_POLICY_CREATED","CANCELLATION_POLICY",saved.getId(),null,saved.getPolicyCode());return saved;}

  @PatchMapping("/{id}")
  public CancellationPolicy update(@RequestHeader(value="Authorization",required=false)String header,@PathVariable Long id,@RequestBody CancellationPolicy input){User u=auth.require(header);auth.requireRole(u,"ADMIN");CancellationPolicy p=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cancellation policy not found"));validate(input);String old=p.getPolicyCode()+"|"+p.getFeePercent()+"|"+p.getStatus();p.setPolicyCode(input.getPolicyCode());p.setAssetType(input.getAssetType());p.setMinHoursBeforeStart(input.getMinHoursBeforeStart());p.setFeePercent(input.getFeePercent());p.setPriority(input.getPriority());p.setStatus(input.getStatus());p.setDescription(input.getDescription());normalize(p);p=repo.save(p);audit.record(u.getId(),"CANCELLATION_POLICY_UPDATED","CANCELLATION_POLICY",id,old,p.getPolicyCode()+"|"+p.getFeePercent()+"|"+p.getStatus());return p;}

  private void normalize(CancellationPolicy p){p.setPolicyCode(p.getPolicyCode().trim().toUpperCase(Locale.ROOT));p.setAssetType(p.getAssetType()==null?"BOTH":p.getAssetType().trim().toUpperCase(Locale.ROOT));p.setStatus(p.getStatus()==null?"ACTIVE":p.getStatus().trim().toUpperCase(Locale.ROOT));if(p.getPriority()==null)p.setPriority(100);}
  private void validate(CancellationPolicy p){if(p.getPolicyCode()==null||p.getPolicyCode().isBlank()||p.getMinHoursBeforeStart()==null||p.getFeePercent()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Policy code, minimum hours and fee percent are required");String type=p.getAssetType()==null?"BOTH":p.getAssetType().trim().toUpperCase(Locale.ROOT);if(!List.of("CAR","PROPERTY","BOTH").contains(type))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"assetType must be CAR, PROPERTY or BOTH");if(p.getFeePercent().compareTo(BigDecimal.ZERO)<0||p.getFeePercent().compareTo(BigDecimal.valueOf(100))>0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"feePercent must be between 0 and 100");}
}
