package com.renthub.availability;

import com.renthub.booking.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvailabilityPolicyServiceTest {
  @Test
  void refusesOwnerBlockThatOverlapsActiveBooking(){
    BookingRepository bookings=mock(BookingRepository.class);AvailabilityBlockRepository blocks=mock(AvailabilityBlockRepository.class);
    when(bookings.existsByAssetTypeAndAssetIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(eq("CAR"),eq(9L),anyCollection(),any(),any())).thenReturn(true);
    AvailabilityPolicyService service=new AvailabilityPolicyService(bookings,blocks);
    assertThrows(ResponseStatusException.class,()->service.assertCanBlock("CAR",9L,LocalDate.now().plusDays(2),LocalDate.now().plusDays(4)));
    verifyNoInteractions(blocks);
  }

  @Test
  void createsBlockWhenPeriodIsFree(){
    BookingRepository bookings=mock(BookingRepository.class);AvailabilityBlockRepository blocks=mock(AvailabilityBlockRepository.class);
    when(blocks.save(any())).thenAnswer(i->{AvailabilityBlock b=i.getArgument(0);return b;});
    AvailabilityPolicyService service=new AvailabilityPolicyService(bookings,blocks);
    AvailabilityBlock saved=service.create("property",4L,LocalDate.now().plusDays(2),LocalDate.now().plusDays(3),"Owner trip","OWNER_BLOCK",10L);
    assertEquals("PROPERTY",saved.getAssetType());assertEquals("OWNER_BLOCK",saved.getBlockType());verify(blocks).save(any());
  }
}
