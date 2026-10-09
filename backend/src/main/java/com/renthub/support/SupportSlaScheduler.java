package com.renthub.support;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SupportSlaScheduler {
  private final SupportService support;

  public SupportSlaScheduler(SupportService support) {
    this.support = support;
  }

  @Scheduled(fixedDelay = 900000)
  public void refresh() {
    support.refreshSlaStates();
  }
}
