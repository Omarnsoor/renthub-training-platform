package com.renthub.support;

import com.renthub.settings.SystemConfigRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupportSlaServiceTest {
  @Test
  void urgentDefaultsToTwoHourResponseWindow(){
    SupportSlaService service=new SupportSlaService(mock(SystemConfigRepository.class));
    LocalDateTime opened=LocalDateTime.of(2026,10,9,10,0);
    assertEquals(opened.plusHours(2),service.dueAt("URGENT",opened));
  }

  @Test
  void firstResponseAfterDeadlineIsBreached(){
    SupportSlaService service=new SupportSlaService(mock(SystemConfigRepository.class));
    SupportTicket ticket=new SupportTicket();
    ticket.setSlaDueAt(LocalDateTime.of(2026,10,9,12,0));
    ticket.setFirstResponseAt(LocalDateTime.of(2026,10,9,12,5));
    assertEquals("BREACHED",service.status(ticket,LocalDateTime.of(2026,10,9,12,10)));
  }
}
