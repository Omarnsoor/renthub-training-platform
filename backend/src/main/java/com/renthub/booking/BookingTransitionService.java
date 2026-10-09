package com.renthub.booking;

import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class BookingTransitionService {
  private final BookingRepository bookings;
  private final BookingHistoryRepository history;

  public BookingTransitionService(BookingRepository bookings, BookingHistoryRepository history) {
    this.bookings = bookings;
    this.history = history;
  }

  public Booking recordCreated(Booking booking, Long actorUserId, String reason) {
    record(booking.getId(), null, booking.getStatus(), actorUserId, reason);
    return booking;
  }

  public Booking transition(Booking booking, Long actorUserId, String newStatus, String reason) {
    String target = normalize(newStatus);
    String old = normalize(booking.getStatus());
    if (old.equals(target)) return booking;
    booking.setStatus(target);
    Booking saved = bookings.save(booking);
    record(saved.getId(), old, target, actorUserId, reason);
    return saved;
  }

  private void record(Long bookingId, String oldStatus, String newStatus, Long actorUserId, String reason) {
    BookingHistory event = new BookingHistory();
    event.setBookingId(bookingId);
    event.setOldStatus(oldStatus);
    event.setNewStatus(newStatus);
    event.setChangedBy(actorUserId);
    event.setReason(reason);
    history.save(event);
  }

  private String normalize(String status) {
    return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
  }
}
