package com.renthub.support;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="RH_TICKET_MESSAGES")
public class TicketMessage {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ticket_message_seq") @SequenceGenerator(name="ticket_message_seq",sequenceName="RH_TICKET_MESSAGE_SEQ",allocationSize=1) private Long id;
 private Long ticketId; private Long senderUserId; private String message; private Integer internalNote=0; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getTicketId(){return ticketId;} public void setTicketId(Long v){ticketId=v;} public Long getSenderUserId(){return senderUserId;} public void setSenderUserId(Long v){senderUserId=v;}
 public String getMessage(){return message;} public void setMessage(String v){message=v;} public Integer getInternalNote(){return internalNote;} public void setInternalNote(Integer v){internalNote=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
