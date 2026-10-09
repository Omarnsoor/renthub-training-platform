package com.renthub.auth;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="RH_AUTH_SESSIONS")
public class AuthSession {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="auth_seq") @SequenceGenerator(name="auth_seq",sequenceName="RH_AUTH_SESSION_SEQ",allocationSize=1) private Long id;
 @Column(nullable=false,unique=true,length=100) private String token; @Column(nullable=false) private Long userId; @Column(nullable=false) private LocalDateTime expiresAt; @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public String getToken(){return token;} public void setToken(String v){token=v;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;} public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime v){expiresAt=v;}
}
