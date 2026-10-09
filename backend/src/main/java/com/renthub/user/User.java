package com.renthub.user;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="RH_USERS")
public class User {
  @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="user_seq")
  @SequenceGenerator(name="user_seq",sequenceName="RH_USER_SEQ",allocationSize=1)
  private Long id;
  @Column(nullable=false,length=120) private String fullName;
  @Column(nullable=false,unique=true,length=160) private String email;
  @Column(nullable=false,length=200) private String passwordHash;
  @Column(nullable=false,length=30) private String role = "CUSTOMER";
  @Column(nullable=false,length=20) private String status = "ACTIVE";
  @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
  public String getEmail(){return email;} public void setEmail(String v){email=v;}
  public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
  public String getRole(){return role;} public void setRole(String v){role=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
