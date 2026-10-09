package com.renthub.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long>{
  List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
  long countByUserIdAndReadFlag(Long userId,Integer readFlag);
}
