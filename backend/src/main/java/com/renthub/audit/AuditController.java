package com.renthub.audit;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@CrossOrigin(origins="http://localhost:5173")
public class AuditController {
  private final AuditLogRepository repo;
  private final AuthService auth;
  public AuditController(AuditLogRepository repo,AuthService auth){this.repo=repo;this.auth=auth;}

  @GetMapping
  public List<AuditLog> recent(@RequestHeader(value="Authorization",required=false)String header){
    User user=auth.require(header);auth.requireRole(user,"ADMIN");return repo.findTop100ByOrderByCreatedAtDesc();
  }

  @GetMapping("/{entityType}/{entityId}")
  public List<AuditLog> entity(@RequestHeader(value="Authorization",required=false)String header,@PathVariable String entityType,@PathVariable Long entityId){
    User user=auth.require(header);auth.requireRole(user,"ADMIN");return repo.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType.toUpperCase(),entityId);
  }
}
