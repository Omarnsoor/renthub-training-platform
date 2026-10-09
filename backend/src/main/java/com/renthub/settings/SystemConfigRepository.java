package com.renthub.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemConfigRepository extends JpaRepository<SystemConfig,String>{
 List<SystemConfig> findByCategoryOrderByConfigKey(String category);
}
