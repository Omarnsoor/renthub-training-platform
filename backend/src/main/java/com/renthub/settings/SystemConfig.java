package com.renthub.settings;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="RH_SYSTEM_CONFIG")
public class SystemConfig {
 @Id private String configKey;
 private String configValue; private String valueType; private String category; private String description; private LocalDateTime updatedAt=LocalDateTime.now(); private Long updatedBy;
 public String getConfigKey(){return configKey;} public void setConfigKey(String v){configKey=v;} public String getConfigValue(){return configValue;} public void setConfigValue(String v){configValue=v;}
 public String getValueType(){return valueType;} public void setValueType(String v){valueType=v;} public String getCategory(){return category;} public void setCategory(String v){category=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;} public Long getUpdatedBy(){return updatedBy;} public void setUpdatedBy(Long v){updatedBy=v;}
}
