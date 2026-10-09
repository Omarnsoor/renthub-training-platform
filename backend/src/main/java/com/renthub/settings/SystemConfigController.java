package com.renthub.settings;

import com.renthub.audit.AuditService;
import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@CrossOrigin(origins="http://localhost:5173")
public class SystemConfigController {
 private final SystemConfigRepository repo; private final AuthService auth; private final AuditService audit;
 public SystemConfigController(SystemConfigRepository repo,AuthService auth,AuditService audit){this.repo=repo;this.auth=auth;this.audit=audit;}
 @GetMapping public List<SystemConfig> all(@RequestHeader(value="Authorization",required=false)String h,@RequestParam(required=false)String category){User u=auth.require(h);auth.requireRole(u,"ADMIN");return category==null?repo.findAll():repo.findByCategoryOrderByConfigKey(category.toUpperCase());}
 @PatchMapping("/{key}") public SystemConfig update(@RequestHeader(value="Authorization",required=false)String h,@PathVariable String key,@RequestBody SystemConfig input){User u=auth.require(h);auth.requireRole(u,"ADMIN");SystemConfig c=repo.findById(key).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Config key not found"));String old=c.getConfigValue();c.setConfigValue(input.getConfigValue());c.setUpdatedAt(LocalDateTime.now());c.setUpdatedBy(u.getId());repo.save(c);audit.record(u.getId(),"CONFIG_UPDATED","SYSTEM_CONFIG",null,old,c.getConfigValue());return c;}
}
