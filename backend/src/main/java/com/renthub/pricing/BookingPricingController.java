package com.renthub.pricing;

import com.renthub.auth.AuthService;
import com.renthub.booking.BookingRepository;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/bookings/{bookingId}/pricing")
@CrossOrigin(origins="http://localhost:5173")
public class BookingPricingController {
  private final BookingRepository bookings; private final PricingSnapshotService snapshots; private final AuthService auth;
  public BookingPricingController(BookingRepository bookings,PricingSnapshotService snapshots,AuthService auth){this.bookings=bookings;this.snapshots=snapshots;this.auth=auth;}

  @GetMapping
  public List<BookingPriceAdjustment> pricing(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long bookingId){
    User u=auth.require(h);var b=bookings.findById(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));
    if(!b.getUserId().equals(u.getId())&&!"ADMIN".equalsIgnoreCase(u.getRole()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");
    return snapshots.forBooking(bookingId);
  }
}
