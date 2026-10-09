package com.renthub.service;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="RH_EXTRA_SERVICES")
public class ExtraService {
  @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="svc_seq")
  @SequenceGenerator(name="svc_seq",sequenceName="RH_SERVICE_SEQ",allocationSize=1)
  private Long id;
  @Column(nullable=false,length=120) private String name;
  @Column(nullable=false,length=40) private String category;
  @Column(nullable=false,precision=12,scale=2) private BigDecimal price;
  @Column(length=500) private String description;
  @Column(length=500) private String imageUrl;
  @Column(nullable=false,length=20) private String status="ACTIVE";
  @Column(name="APPLICABLE_TO",nullable=false,length=20) private String applicableTo="BOTH";
  public Long getId(){return id;} public void setId(Long v){id=v;}
  public String getName(){return name;} public void setName(String v){name=v;}
  public String getCategory(){return category;} public void setCategory(String v){category=v;}
  public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
  public String getDescription(){return description;} public void setDescription(String v){description=v;}
  public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public String getApplicableTo(){return applicableTo;} public void setApplicableTo(String v){applicableTo=v;}
}
