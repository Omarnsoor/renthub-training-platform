package com.renthub.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
  private final NotificationRepository repo;
  public NotificationService(NotificationRepository repo){this.repo=repo;}

  @Transactional
  public Notification send(Long userId,String type,String title,String message,String referenceType,Long referenceId){
    Notification n=new Notification();n.setUserId(userId);n.setType(type);n.setTitle(title);n.setMessage(message);n.setReferenceType(referenceType);n.setReferenceId(referenceId);return repo.save(n);
  }
}
