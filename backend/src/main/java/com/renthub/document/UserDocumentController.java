package com.renthub.document;

import com.renthub.audit.AuditService;
import com.renthub.auth.AuthService;
import com.renthub.notification.NotificationService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins="http://localhost:5173")
public class UserDocumentController {
 private final UserDocumentRepository repo; private final AuthService auth; private final AuditService audit; private final NotificationService notifications;
 public UserDocumentController(UserDocumentRepository repo,AuthService auth,AuditService audit,NotificationService notifications){this.repo=repo;this.auth=auth;this.audit=audit;this.notifications=notifications;}
 @GetMapping("/mine") public List<UserDocument> mine(@RequestHeader(value="Authorization",required=false)String h){return repo.findByUserIdOrderByCreatedAtDesc(auth.require(h).getId());}
 @PostMapping public UserDocument uploadMetadata(@RequestHeader(value="Authorization",required=false)String h,@RequestBody UserDocument d){User u=auth.require(h);d.setUserId(u.getId());d.setVerificationStatus("PENDING");if(d.getDocumentType()==null||d.getFileName()==null||d.getStorageKey()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Document type, file name and storage key are required");UserDocument saved=repo.save(d);audit.record(u.getId(),"DOCUMENT_SUBMITTED","USER_DOCUMENT",saved.getId(),null,d.getDocumentType());return saved;}
 @GetMapping("/admin") public List<UserDocument> admin(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"ADMIN");return repo.findAll();}
 @PatchMapping("/{id}/verify/{status}") public UserDocument verify(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@PathVariable String status){User u=auth.require(h);auth.requireRole(u,"ADMIN");UserDocument d=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Document not found"));String old=d.getVerificationStatus();d.setVerificationStatus(status.toUpperCase());repo.save(d);audit.record(u.getId(),"DOCUMENT_STATUS","USER_DOCUMENT",id,old,d.getVerificationStatus());notifications.send(d.getUserId(),"ACCOUNT","Document status updated",d.getDocumentType()+" is now "+d.getVerificationStatus(),"USER_DOCUMENT",id);return d;}
}
