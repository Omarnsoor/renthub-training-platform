package com.renthub.bookingservice;

import com.renthub.auth.AuthService;
import com.renthub.booking.Booking;
import com.renthub.booking.BookingRepository;
import com.renthub.service.ExtraService;
import com.renthub.service.ExtraServiceRepository;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/booking-services")
@CrossOrigin(origins="http://localhost:5173")
public class BookingServiceAddonController {
  private final BookingServiceAddonRepository repo;
  private final BookingRepository bookings;
  private final ExtraServiceRepository services;
  private final AuthService auth;

  public BookingServiceAddonController(
      BookingServiceAddonRepository repo,
      BookingRepository bookings,
      ExtraServiceRepository services,
      AuthService auth
  ) {
    this.repo = repo;
    this.bookings = bookings;
    this.services = services;
    this.auth = auth;
  }

  @GetMapping("/{bookingId}")
  public List<BookingServiceAddon> list(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long bookingId
  ) {
    User user = auth.require(header);
    Booking booking = booking(bookingId);
    if (!booking.getUserId().equals(user.getId()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
    }
    return repo.findByBookingId(bookingId);
  }

  @PostMapping("/{bookingId}/{serviceId}")
  public BookingServiceAddon add(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long bookingId,
      @PathVariable Long serviceId
  ) {
    User user = auth.require(header);
    Booking booking = ownedPendingBooking(user, bookingId);
    if (repo.existsByBookingIdAndServiceId(bookingId, serviceId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Service already added");
    }
    ExtraService service = services.findById(serviceId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    if (!"ACTIVE".equalsIgnoreCase(service.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Service is not active");
    }
    BookingServiceAddon addon = new BookingServiceAddon();
    addon.setBookingId(booking.getId());
    addon.setServiceId(serviceId);
    addon.setPrice(service.getPrice());
    return repo.save(addon);
  }

  @DeleteMapping("/{bookingId}/{serviceId}")
  public void remove(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long bookingId,
      @PathVariable Long serviceId
  ) {
    User user = auth.require(header);
    ownedPendingBooking(user, bookingId);
    BookingServiceAddon addon = repo.findByBookingIdAndServiceId(bookingId, serviceId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service is not attached to this booking"));
    repo.delete(addon);
  }

  private Booking booking(Long id) {
    return bookings.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
  }

  private Booking ownedPendingBooking(User user, Long id) {
    Booking booking = booking(id);
    if (!booking.getUserId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
    }
    if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Services can only be changed before payment");
    }
    return booking;
  }
}
