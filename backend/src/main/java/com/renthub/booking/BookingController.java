package com.renthub.booking;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {
  private final BookingRepository repository;
  private final BookingService service;

  public BookingController(BookingRepository repository, BookingService service) {
    this.repository = repository;
    this.service = service;
  }

  @GetMapping
  public List<Booking> all() {
    return repository.findAll();
  }

  @GetMapping("/user/{userId}")
  public List<Booking> byUser(@PathVariable Long userId) {
    return repository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @PostMapping
  public Booking create(@Valid @RequestBody BookingRequest request) {
    return service.create(request);
  }

  @PatchMapping("/{id}/cancel")
  public Booking cancel(@PathVariable Long id) {
    return service.cancel(id);
  }
}
