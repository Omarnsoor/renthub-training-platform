package com.renthub.support;

import com.renthub.settings.SystemConfigRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SupportSlaService {
  private final SystemConfigRepository config;

  public SupportSlaService(SystemConfigRepository config) {
    this.config = config;
  }

  public int responseHours(String rawPriority) {
    String priority = rawPriority == null || rawPriority.isBlank() ? "NORMAL" : rawPriority.trim().toLowerCase();
    int fallback = switch (priority) {
      case "urgent" -> 2;
      case "high" -> 8;
      case "low" -> 48;
      default -> 24;
    };
    return config.findById("support." + priority + ".sla.hours")
        .map(c -> Integer.parseInt(c.getConfigValue()))
        .orElse(fallback);
  }

  public LocalDateTime dueAt(String priority, LocalDateTime openedAt) {
    return openedAt.plusHours(responseHours(priority));
  }

  public String status(SupportTicket ticket, LocalDateTime now) {
    if (ticket.getFirstResponseAt() != null) {
      return ticket.getSlaDueAt() != null && ticket.getFirstResponseAt().isAfter(ticket.getSlaDueAt()) ? "BREACHED" : "MET";
    }
    if (ticket.getSlaDueAt() != null && now.isAfter(ticket.getSlaDueAt())) return "BREACHED";
    return "ON_TRACK";
  }
}
