package com.renthub.audit;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="RH_AUDIT_LOG") public class AuditLog{
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="audit_seq") @SequenceGenerator(name="audit_seq",sequenceName="RH_AUDIT_SEQ",allocationSize=1) private Long id;
 private Long actorUserId; private String action; private String entityType; private Long entityId; private String oldValue; private String newValue; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getActorUserId(){return actorUserId;} public void setActorUserId(Long v){actorUserId=v;} public String getAction(){return action;} public void setAction(String v){action=v;} public String getEntityType(){return entityType;} public void setEntityType(String v){entityType=v;} public Long getEntityId(){return entityId;} public void setEntityId(Long v){entityId=v;} public String getOldValue(){return oldValue;} public void setOldValue(String v){oldValue=v;} public String getNewValue(){return newValue;} public void setNewValue(String v){newValue=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
