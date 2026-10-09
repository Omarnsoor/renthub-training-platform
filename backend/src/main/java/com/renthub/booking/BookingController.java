package com.renthub.booking;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins="http://localhost:5173")
public class BookingController {
  private final BookingRepository repo;
  private final BookingService service;
  private final AuthService auth;

  public BookingController(BookingRepository repo, BookingService service, AuthService auth) {
    this.repo = repo;
    this.service = service;
    this.auth = auth;
  }

  @GetMapping("/availability")
  public Map<String,Object> availability(
      @RequestParam String assetType,
      @RequestParam Long assetId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    return service.availability(assetType, assetId, startDate, endDate);
  }

  @GetMapping("/mine")
  public List<Booking> mine(@RequestHeader(value="Authorization", required=false) String header) {
    return repo.findByUserIdOrderByCreatedAtDesc(auth.require(header).getId());
  }

  @GetMapping
  public List<Booking> all(@RequestHeader(value="Authorization", required=false) String header) {
    User user = auth.require(header);
    auth.requireRole(user, "ADMIN");
    return repo.findAll();
  }

  @PostMapping
  public Booking create(
      @RequestHeader(value="Authorization", required=false) String header,
      @Valid @RequestBody BookingRequest request
  ) {
    return service.create(auth.require(header).getId(), request);
  }

  @PatchMapping("/{id}/cancel")
  public Booking cancel(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id
  ) {
    User user = auth.require(header);
    return service.cancel(user.getId(), id, "ADMIN".equalsIgnoreCase(user.getRole()));
  }
}
