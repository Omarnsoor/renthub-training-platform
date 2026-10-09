package com.renthub.document;

import com.renthub.settings.SystemConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
public class DocumentEligibilityService {
  private final UserDocumentRepository documents;
  private final SystemConfigRepository config;

  public DocumentEligibilityService(UserDocumentRepository documents, SystemConfigRepository config) {
    this.documents = documents;
    this.config = config;
  }

  public void assertCheckInEligible(Long userId, String rawAssetType) {
    String assetType = rawAssetType == null ? "" : rawAssetType.trim().toUpperCase();
    List<String> required = requiredTypes(assetType);
    List<UserDocument> userDocs = documents.findByUserIdOrderByCreatedAtDesc(userId);
    LocalDate today = LocalDate.now();
    boolean eligible = userDocs.stream().anyMatch(d ->
        required.contains(String.valueOf(d.getDocumentType()).toUpperCase()) &&
        "VERIFIED".equalsIgnoreCase(d.getVerificationStatus()) &&
        (d.getExpiryDate() == null || !d.getExpiryDate().isBefore(today))
    );
    if (!eligible) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Check-in requires a verified, non-expired document: " + String.join(" or ", required));
    }
  }

  public List<String> requiredTypes(String rawAssetType) {
    String assetType = rawAssetType == null ? "" : rawAssetType.trim().toUpperCase();
    String key = switch (assetType) {
      case "CAR" -> "documents.car.checkin.required";
      case "PROPERTY" -> "documents.property.checkin.required";
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported asset type");
    };
    String fallback = "CAR".equals(assetType) ? "DRIVING_LICENSE" : "NATIONAL_ID,PASSPORT";
    String value = config.findById(key).map(c -> c.getConfigValue()).orElse(fallback);
    return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isBlank()).map(String::toUpperCase).toList();
  }
}
