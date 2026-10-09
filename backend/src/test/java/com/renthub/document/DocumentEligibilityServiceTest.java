package com.renthub.document;

import com.renthub.settings.SystemConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentEligibilityServiceTest {
  @Test
  void carCheckinRequiresVerifiedNonExpiredDrivingLicense(){
    UserDocumentRepository docs=mock(UserDocumentRepository.class);SystemConfigRepository config=mock(SystemConfigRepository.class);
    UserDocument d=new UserDocument();d.setUserId(5L);d.setDocumentType("DRIVING_LICENSE");d.setVerificationStatus("VERIFIED");d.setExpiryDate(LocalDate.now().plusYears(1));
    when(docs.findByUserIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(d));
    DocumentEligibilityService service=new DocumentEligibilityService(docs,config);
    assertDoesNotThrow(()->service.assertCheckInEligible(5L,"CAR"));
  }

  @Test
  void expiredDocumentDoesNotSatisfyCheckinRule(){
    UserDocumentRepository docs=mock(UserDocumentRepository.class);SystemConfigRepository config=mock(SystemConfigRepository.class);
    UserDocument d=new UserDocument();d.setUserId(5L);d.setDocumentType("PASSPORT");d.setVerificationStatus("VERIFIED");d.setExpiryDate(LocalDate.now().minusDays(1));
    when(docs.findByUserIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(d));
    DocumentEligibilityService service=new DocumentEligibilityService(docs,config);
    assertThrows(ResponseStatusException.class,()->service.assertCheckInEligible(5L,"PROPERTY"));
  }
}
