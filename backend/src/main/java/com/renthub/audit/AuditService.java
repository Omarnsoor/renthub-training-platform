package com.renthub.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditLogRepository repo;
  public AuditService(AuditLogRepository repo){this.repo=repo;}

  @Transactional
  public void record(Long actorUserId,String action,String entityType,Long entityId,Object oldValue,Object newValue){
    AuditLog log=new AuditLog();
    log.setActorUserId(actorUserId);
    log.setAction(action);
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    log.setOldValue(compact(oldValue));
    log.setNewValue(compact(newValue));
    repo.save(log);
  }

  private String compact(Object value){
    if(value==null)return null;
    String text=String.valueOf(value);
    return text.length()>1900?text.substring(0,1900):text;
  }
}
