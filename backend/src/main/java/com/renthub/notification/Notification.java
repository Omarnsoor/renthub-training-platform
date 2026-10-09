package com.renthub.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="RH_NOTIFICATIONS")
public class Notification {
  @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="notification_seq")
  @SequenceGenerator(name="notification_seq",sequenceName="RH_NOTIFICATION_SEQ",allocationSize=1)
  private Long id;
  private Long userId;
  private String type;
  private String title;
  private String message;
  private String referenceType;
  private Long referenceId;
  @Column(name="READ_FLAG") private Integer readFlag=0;
  private LocalDateTime createdAt=LocalDateTime.now();
  public Long getId(){return id;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
  public String getType(){return type;} public void setType(String v){type=v;} public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public String getMessage(){return message;} public void setMessage(String v){message=v;} public String getReferenceType(){return referenceType;} public void setReferenceType(String v){referenceType=v;}
  public Long getReferenceId(){return referenceId;} public void setReferenceId(Long v){referenceId=v;} public Integer getReadFlag(){return readFlag;} public void setReadFlag(Integer v){readFlag=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}
