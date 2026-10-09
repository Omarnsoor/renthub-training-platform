package com.renthub.dashboard;

import com.renthub.auth.AuthService;
import com.renthub.booking.Booking;
import com.renthub.booking.BookingRepository;
import com.renthub.car.Car;
import com.renthub.car.CarRepository;
import com.renthub.payment.Payment;
import com.renthub.payment.PaymentRepository;
import com.renthub.property.Property;
import com.renthub.property.PropertyRepository;
import com.renthub.user.User;
import com.renthub.user.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins="http://localhost:5173")
public class DashboardController {
  private final AuthService auth;
  private final BookingRepository bookings;
  private final UserRepository users;
  private final CarRepository cars;
  private final PropertyRepository properties;
  private final PaymentRepository payments;

  public DashboardController(
      AuthService auth,
      BookingRepository bookings,
      UserRepository users,
      CarRepository cars,
      PropertyRepository properties,
      PaymentRepository payments
  ) {
    this.auth = auth;
    this.bookings = bookings;
    this.users = users;
    this.cars = cars;
    this.properties = properties;
    this.payments = payments;
  }

  @GetMapping("/admin")
  public Map<String,Object> admin(@RequestHeader(value="Authorization", required=false) String header) {
    User user = auth.require(header);
    auth.requireRole(user, "ADMIN");
    List<Booking> allBookings = bookings.findAll();
    BigDecimal revenue = payments.findAll().stream()
        .filter(p -> "PAID".equalsIgnoreCase(p.getStatus()))
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return ordered(
        "users", users.count(),
        "cars", cars.count(),
        "properties", properties.count(),
        "bookings", allBookings.size(),
        "pendingBookings", countStatus(allBookings, "PENDING"),
        "paidBookings", countStatus(allBookings, "PAID"),
        "cancelledBookings", countStatus(allBookings, "CANCELLED"),
        "revenue", revenue
    );
  }

  @GetMapping("/owner")
  public Map<String,Object> owner(@RequestHeader(value="Authorization", required=false) String header) {
    User user = auth.require(header);
    auth.requireRole(user, "OWNER", "ADMIN");

    Set<Long> carIds = cars.findAll().stream()
        .filter(car -> user.getId().equals(car.getOwnerId()))
        .map(Car::getId)
        .collect(Collectors.toSet());
    Set<Long> propertyIds = properties.findAll().stream()
        .filter(property -> user.getId().equals(property.getOwnerId()))
        .map(Property::getId)
        .collect(Collectors.toSet());

    List<Booking> ownerBookings = bookings.findAll().stream()
        .filter(b -> ("CAR".equalsIgnoreCase(b.getAssetType()) && carIds.contains(b.getAssetId()))
            || ("PROPERTY".equalsIgnoreCase(b.getAssetType()) && propertyIds.contains(b.getAssetId())))
        .toList();
    Set<Long> ownerBookingIds = ownerBookings.stream().map(Booking::getId).collect(Collectors.toSet());
    BigDecimal revenue = payments.findAll().stream()
        .filter(p -> ownerBookingIds.contains(p.getBookingId()) && "PAID".equalsIgnoreCase(p.getStatus()))
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return ordered(
        "ownedCars", carIds.size(),
        "ownedProperties", propertyIds.size(),
        "bookings", ownerBookings.size(),
        "pendingBookings", countStatus(ownerBookings, "PENDING"),
        "paidBookings", countStatus(ownerBookings, "PAID"),
        "revenue", revenue
    );
  }

  private long countStatus(List<Booking> source, String status) {
    return source.stream().filter(b -> status.equalsIgnoreCase(b.getStatus())).count();
  }

  private Map<String,Object> ordered(Object... values) {
    Map<String,Object> result = new LinkedHashMap<>();
    for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
    return result;
  }
}
