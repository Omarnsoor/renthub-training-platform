package com.renthub.document;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name="RH_USER_DOCUMENTS")
public class UserDocument {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="document_seq") @SequenceGenerator(name="document_seq",sequenceName="RH_DOCUMENT_SEQ",allocationSize=1) private Long id;
 private Long userId; private String documentType; private String fileName; private String storageKey; private String verificationStatus="PENDING"; private LocalDate expiryDate; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;} public String getDocumentType(){return documentType;} public void setDocumentType(String v){documentType=v;} public String getFileName(){return fileName;} public void setFileName(String v){fileName=v;} public String getStorageKey(){return storageKey;} public void setStorageKey(String v){storageKey=v;}
 public String getVerificationStatus(){return verificationStatus;} public void setVerificationStatus(String v){verificationStatus=v;} public LocalDate getExpiryDate(){return expiryDate;} public void setExpiryDate(LocalDate v){expiryDate=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
