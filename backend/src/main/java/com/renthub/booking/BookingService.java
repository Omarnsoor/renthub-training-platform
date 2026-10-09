package com.renthub.booking;

import com.renthub.car.Car;
import com.renthub.car.CarRepository;
import com.renthub.property.Property;
import com.renthub.property.PropertyRepository;
import com.renthub.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {
  private static final List<String> BLOCKING_STATUSES = List.of("PENDING", "CONFIRMED", "PAID");

  private final BookingRepository bookingRepository;
  private final CarRepository carRepository;
  private final PropertyRepository propertyRepository;
  private final UserRepository userRepository;

  public BookingService(
      BookingRepository bookingRepository,
      CarRepository carRepository,
      PropertyRepository propertyRepository,
      UserRepository userRepository
  ) {
    this.bookingRepository = bookingRepository;
    this.carRepository = carRepository;
    this.propertyRepository = propertyRepository;
    this.userRepository = userRepository;
  }

  public Booking create(BookingRequest request) {
    if (!request.endDate().isAfter(request.startDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be after startDate");
    }
    if (!userRepository.existsById(request.userId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    String assetType = request.assetType().trim().toUpperCase();
    BigDecimal unitRate = switch (assetType) {
      case "CAR" -> carRate(request.assetId());
      case "PROPERTY" -> propertyRate(request.assetId());
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetType must be CAR or PROPERTY");
    };

    boolean overlaps = bookingRepository
        .existsByAssetTypeAndAssetIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
            assetType,
            request.assetId(),
            BLOCKING_STATUSES,
            request.endDate(),
            request.startDate()
        );
    if (overlaps) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset is already booked for the selected dates");
    }

    long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
    Booking booking = new Booking();
    booking.setUserId(request.userId());
    booking.setAssetType(assetType);
    booking.setAssetId(request.assetId());
    booking.setStartDate(request.startDate());
    booking.setEndDate(request.endDate());
    booking.setTotalAmount(unitRate.multiply(BigDecimal.valueOf(days)));
    booking.setStatus("PENDING");
    return bookingRepository.save(booking);
  }

  public Booking cancel(Long id) {
    Booking booking = bookingRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    if ("CANCELLED".equals(booking.getStatus())) {
      return booking;
    }
    booking.setStatus("CANCELLED");
    return bookingRepository.save(booking);
  }

  private BigDecimal carRate(Long id) {
    Car car = carRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
    if (!"AVAILABLE".equalsIgnoreCase(car.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Car is not available");
    }
    return car.getDailyRate();
  }

  private BigDecimal propertyRate(Long id) {
    Property property = propertyRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    if (!"AVAILABLE".equalsIgnoreCase(property.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Property is not available");
    }
    return property.getNightlyRate();
  }
}
