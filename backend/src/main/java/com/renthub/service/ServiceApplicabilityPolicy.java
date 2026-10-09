package com.renthub.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Component
public class ServiceApplicabilityPolicy {
  private static final Set<String> ASSET_TYPES = Set.of("CAR", "PROPERTY");
  private static final Set<String> TARGETS = Set.of("CAR", "PROPERTY", "BOTH");

  public String normalizeAssetType(String raw) {
    String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    if (!ASSET_TYPES.contains(value)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetType must be CAR or PROPERTY");
    }
    return value;
  }

  public String normalizeApplicableTo(String raw) {
    String value = raw == null || raw.isBlank() ? "BOTH" : raw.trim().toUpperCase(Locale.ROOT);
    if (!TARGETS.contains(value)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "applicableTo must be CAR, PROPERTY or BOTH");
    }
    return value;
  }

  public boolean allows(ExtraService service, String rawAssetType) {
    String assetType = normalizeAssetType(rawAssetType);
    String target = normalizeApplicableTo(service.getApplicableTo());
    return "BOTH".equals(target) || assetType.equals(target);
  }

  public void requireAllowed(ExtraService service, String rawAssetType) {
    if (!allows(service, rawAssetType)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Service '" + service.getName() + "' is not available for " + normalizeAssetType(rawAssetType) + " bookings"
      );
    }
  }
}
