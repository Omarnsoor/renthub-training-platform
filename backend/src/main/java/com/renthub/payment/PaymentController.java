package com.renthub.payment;

import com.renthub.auth.AuthService;
import com.renthub.booking.Booking;
import com.renthub.booking.BookingRepository;
import com.renthub.bookingservice.BookingServiceAddon;
import com.renthub.bookingservice.BookingServiceAddonRepository;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins="http://localhost:5173")
public class PaymentController {
  private final PaymentRepository repo;
  private final BookingRepository bookings;
  private final BookingServiceAddonRepository addons;
  private final AuthService auth;

  public PaymentController(
      PaymentRepository repo,
      BookingRepository bookings,
      BookingServiceAddonRepository addons,
      AuthService auth
  ) {
    this.repo = repo;
    this.bookings = bookings;
    this.addons = addons;
    this.auth = auth;
  }

  @GetMapping("/booking/{id}")
  public Payment receipt(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id
  ) {
    User user = auth.require(header);
    Booking booking = booking(id);
    if (!booking.getUserId().equals(user.getId()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
    }
    return repo.findByBookingId(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
  }

  @PostMapping("/booking/{id}")
  public Payment pay(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id,
      @RequestBody(required=false) Map<String,Object> body
  ) {
    User user = auth.require(header);
    Booking booking = booking(id);
    if (!booking.getUserId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
    }
    if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled booking cannot be paid");
    }
    if ("PAID".equalsIgnoreCase(booking.getStatus())) {
      return repo.findByBookingId(id)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Booking is already paid"));
    }

    BigDecimal extras = addons.findByBookingId(id).stream()
        .map(BookingServiceAddon::getPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal total = booking.getTotalAmount().add(extras);

    Payment payment = repo.findByBookingId(id).orElseGet(Payment::new);
    payment.setBookingId(id);
    payment.setUserId(user.getId());
    payment.setAmount(total);
    payment.setMethod(body == null ? "CARD" : String.valueOf(body.getOrDefault("method", "CARD")).toUpperCase());
    payment.setStatus("PAID");
    payment.setReferenceNo("RH-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
    payment = repo.save(payment);

    booking.setStatus("PAID");
    bookings.save(booking);
    return payment;
  }

  private Booking booking(Long id) {
    return bookings.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
  }
}
