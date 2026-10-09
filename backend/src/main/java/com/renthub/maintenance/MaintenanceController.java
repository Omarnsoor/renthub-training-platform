package com.renthub.maintenance;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins="http://localhost:5173")
public class MaintenanceController {
 private final MaintenanceRepository repo; private final MaintenanceService service; private final AuthService auth;
 public MaintenanceController(MaintenanceRepository repo,MaintenanceService service,AuthService auth){this.repo=repo;this.service=service;this.auth=auth;}
 @GetMapping("/mine") public List<MaintenanceRecord> mine(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"OWNER","ADMIN");return "ADMIN".equalsIgnoreCase(u.getRole())?repo.findAll():repo.findByOwnerIdOrderByCreatedAtDesc(u.getId());}
 @PostMapping public MaintenanceRecord create(@RequestHeader(value="Authorization",required=false)String h,@RequestBody MaintenanceRecord record){User u=auth.require(h);auth.requireRole(u,"OWNER","ADMIN");return service.schedule(u,record);}
 @PatchMapping("/{id}/status/{status}") public MaintenanceRecord status(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@PathVariable String status){User u=auth.require(h);auth.requireRole(u,"OWNER","ADMIN");return service.status(u,id,status);}
}
