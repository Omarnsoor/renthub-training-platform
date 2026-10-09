package com.renthub.booking;

import com.renthub.car.Car;
import com.renthub.car.CarRepository;
import com.renthub.property.Property;
import com.renthub.property.PropertyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {
  private static final List<String> BLOCKING = List.of("PENDING", "CONFIRMED", "PAID");
  private final BookingRepository repo;
  private final CarRepository cars;
  private final PropertyRepository props;

  public BookingService(BookingRepository repo, CarRepository cars, PropertyRepository props) {
    this.repo = repo;
    this.cars = cars;
    this.props = props;
  }

  public Booking create(Long userId, BookingRequest request) {
    validateDates(request.startDate(), request.endDate());
    String type = normalizeType(request.assetType());
    BigDecimal rate = rate(type, request.assetId());
    if (!isAvailable(type, request.assetId(), request.startDate(), request.endDate())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This listing is already booked for the selected dates");
    }

    long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
    Booking booking = new Booking();
    booking.setUserId(userId);
    booking.setAssetType(type);
    booking.setAssetId(request.assetId());
    booking.setStartDate(request.startDate());
    booking.setEndDate(request.endDate());
    booking.setTotalAmount(rate.multiply(BigDecimal.valueOf(days)));
    booking.setStatus("PENDING");
    return repo.save(booking);
  }

  public Map<String,Object> availability(String rawType, Long assetId, LocalDate startDate, LocalDate endDate) {
    validateDates(startDate, endDate);
    String type = normalizeType(rawType);
    BigDecimal rate = rate(type, assetId);
    boolean available = isAvailable(type, assetId, startDate, endDate);
    long days = ChronoUnit.DAYS.between(startDate, endDate);
    return Map.of(
        "available", available,
        "days", days,
        "unitRate", rate,
        "estimatedTotal", rate.multiply(BigDecimal.valueOf(days))
    );
  }

  public Booking cancel(Long userId, Long id, boolean admin) {
    Booking booking = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    if (!admin && !booking.getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
    }
    if ("PAID".equalsIgnoreCase(booking.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Paid booking cannot be cancelled in this demo flow");
    }
    if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
      return booking;
    }
    booking.setStatus("CANCELLED");
    return repo.save(booking);
  }

  private boolean isAvailable(String type, Long assetId, LocalDate startDate, LocalDate endDate) {
    return !repo.existsByAssetTypeAndAssetIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
        type, assetId, BLOCKING, endDate, startDate);
  }

  private void validateDates(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end dates are required");
    }
    if (startDate.isBefore(LocalDate.now())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be in the past");
    }
    if (!endDate.isAfter(startDate)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
    }
  }

  private String normalizeType(String type) {
    String normalized = type == null ? "" : type.trim().toUpperCase();
    if (!List.of("CAR", "PROPERTY").contains(normalized)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetType must be CAR or PROPERTY");
    }
    return normalized;
  }

  private BigDecimal rate(String type, Long id) {
    return switch (type) {
      case "CAR" -> carRate(id);
      case "PROPERTY" -> propertyRate(id);
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported asset type");
    };
  }

  private BigDecimal carRate(Long id) {
    Car car = cars.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
    if (!"AVAILABLE".equalsIgnoreCase(car.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Car is not available");
    }
    return car.getDailyRate();
  }

  private BigDecimal propertyRate(Long id) {
    Property property = props.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    if (!"AVAILABLE".equalsIgnoreCase(property.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Property is not available");
    }
    return property.getNightlyRate();
  }
}
