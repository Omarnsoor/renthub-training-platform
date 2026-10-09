package com.renthub.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class ServiceApplicabilityPolicyTest {
  private final ServiceApplicabilityPolicy policy = new ServiceApplicabilityPolicy();

  @Test
  void carOnlyServiceAllowsCarAndRejectsProperty() {
    ExtraService service = new ExtraService();
    service.setName("Baby Seat");
    service.setApplicableTo("CAR");
    assertTrue(policy.allows(service,"CAR"));
    assertFalse(policy.allows(service,"PROPERTY"));
    assertThrows(ResponseStatusException.class,()->policy.requireAllowed(service,"PROPERTY"));
  }

  @Test
  void bothServiceWorksForEverySupportedBookingType() {
    ExtraService service = new ExtraService();
    service.setApplicableTo("BOTH");
    assertTrue(policy.allows(service,"CAR"));
    assertTrue(policy.allows(service,"PROPERTY"));
  }

  @Test
  void invalidApplicabilityCannotEnterTheDomain() {
    assertThrows(ResponseStatusException.class,()->policy.normalizeApplicableTo("BOAT"));
    assertThrows(ResponseStatusException.class,()->policy.normalizeAssetType("HOTEL"));
  }
}
