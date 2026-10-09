package com.renthub.review;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="RH_REVIEWS")
public class Review {
  @Id
  @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="rev_seq")
  @SequenceGenerator(name="rev_seq",sequenceName="RH_REVIEW_SEQ",allocationSize=1)
  private Long id;

  @Column(name="USER_ID", nullable=false)
  private Long userId;

  @Column(name="ASSET_TYPE", nullable=false, length=20)
  private String assetType;

  @Column(name="ASSET_ID", nullable=false)
  private Long assetId;

  @Column(name="RATING", nullable=false)
  private Integer rating;

  @Column(name="REVIEW_COMMENT", length=1000)
  private String comment;

  @Column(name="CREATED_AT", nullable=false)
  private LocalDateTime createdAt=LocalDateTime.now();

  public Long getId(){return id;}
  public Long getUserId(){return userId;}
  public void setUserId(Long v){userId=v;}
  public String getAssetType(){return assetType;}
  public void setAssetType(String v){assetType=v;}
  public Long getAssetId(){return assetId;}
  public void setAssetId(Long v){assetId=v;}
  public Integer getRating(){return rating;}
  public void setRating(Integer v){rating=v;}
  public String getComment(){return comment;}
  public void setComment(String v){comment=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}
