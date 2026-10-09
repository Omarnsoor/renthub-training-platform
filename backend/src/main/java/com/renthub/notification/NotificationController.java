package com.renthub.notification;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins="http://localhost:5173")
public class NotificationController {
  private final NotificationRepository repo; private final AuthService auth;
  public NotificationController(NotificationRepository repo,AuthService auth){this.repo=repo;this.auth=auth;}
  @GetMapping public List<Notification> mine(@RequestHeader(value="Authorization",required=false)String h){return repo.findByUserIdOrderByCreatedAtDesc(auth.require(h).getId());}
  @GetMapping("/summary") public Map<String,Object> summary(@RequestHeader(value="Authorization",required=false)String h){Long id=auth.require(h).getId();return Map.of("unread",repo.countByUserIdAndReadFlag(id,0));}
  @PatchMapping("/{id}/read") public Notification read(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){User u=auth.require(h);Notification n=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));if(!n.getUserId().equals(u.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your notification");n.setReadFlag(1);return repo.save(n);}
  @PatchMapping("/read-all") public Map<String,Object> readAll(@RequestHeader(value="Authorization",required=false)String h){Long id=auth.require(h).getId();List<Notification> list=repo.findByUserIdOrderByCreatedAtDesc(id);list.forEach(n->n.setReadFlag(1));repo.saveAll(list);return Map.of("updated",list.size());}
}
