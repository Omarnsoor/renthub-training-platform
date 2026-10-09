package com.renthub.booking;

import com.renthub.audit.AuditService;
import com.renthub.availability.AvailabilityBlockRepository;
import com.renthub.car.Car;
import com.renthub.car.CarRepository;
import com.renthub.notification.NotificationService;
import com.renthub.pricing.PricingService;
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
  private static final List<String> BLOCKING = List.of("PENDING", "CONFIRMED", "PAID", "REFUND_PENDING", "REFUND_APPROVED", "COMPLETED");
  private final BookingRepository repo; private final CarRepository cars; private final PropertyRepository props; private final PricingService pricing;
  private final AuditService audit; private final NotificationService notifications; private final AvailabilityBlockRepository availabilityBlocks; private final BookingTransitionService transitions;

  public BookingService(BookingRepository repo, CarRepository cars, PropertyRepository props, PricingService pricing, AuditService audit, NotificationService notifications, AvailabilityBlockRepository availabilityBlocks, BookingTransitionService transitions) {
    this.repo=repo;this.cars=cars;this.props=props;this.pricing=pricing;this.audit=audit;this.notifications=notifications;this.availabilityBlocks=availabilityBlocks;this.transitions=transitions;
  }

  public Booking create(Long userId, BookingRequest request) {
    validateDates(request.startDate(), request.endDate()); String type=normalizeType(request.assetType()); ListingPrice lp=listing(type,request.assetId());
    if(!isAvailable(type,request.assetId(),request.startDate(),request.endDate()))throw new ResponseStatusException(HttpStatus.CONFLICT,"This listing is unavailable for the selected dates");
    long days=ChronoUnit.DAYS.between(request.startDate(),request.endDate()); BigDecimal base=lp.rate().multiply(BigDecimal.valueOf(days)); BigDecimal finalAmount=pricing.price(type,lp.city(),request.startDate(),base).finalAmount();
    Booking booking=new Booking();booking.setUserId(userId);booking.setAssetType(type);booking.setAssetId(request.assetId());booking.setStartDate(request.startDate());booking.setEndDate(request.endDate());booking.setTotalAmount(finalAmount);booking.setStatus("PENDING");booking.setDepositAmount(type.equals("CAR")?lp.rate().multiply(BigDecimal.valueOf(2)):BigDecimal.ZERO);booking=repo.save(booking);transitions.recordCreated(booking,userId,"Booking created");
    audit.record(userId,"BOOKING_CREATED","BOOKING",booking.getId(),null,"amount="+finalAmount);notifications.send(userId,"BOOKING","Booking created","Booking #"+booking.getId()+" is waiting for payment","BOOKING",booking.getId());return booking;
  }

  public Map<String,Object> availability(String rawType,Long assetId,LocalDate startDate,LocalDate endDate){
    validateDates(startDate,endDate);String type=normalizeType(rawType);ListingPrice lp=listing(type,assetId);boolean bookingFree=!repo.existsByAssetTypeAndAssetIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(type,assetId,BLOCKING,endDate,startDate);boolean ownerFree=!availabilityBlocks.existsByAssetTypeAndAssetIdAndStartDateLessThanAndEndDateGreaterThan(type,assetId,endDate,startDate);long days=ChronoUnit.DAYS.between(startDate,endDate);BigDecimal base=lp.rate().multiply(BigDecimal.valueOf(days));PricingService.PriceResult priced=pricing.price(type,lp.city(),startDate,base);
    return Map.of("available",bookingFree&&ownerFree,"bookingConflict",!bookingFree,"ownerBlock",!ownerFree,"days",days,"unitRate",lp.rate(),"baseAmount",base,"adjustments",priced.adjustments(),"estimatedTotal",priced.finalAmount());
  }

  public Booking cancel(Long userId,Long id,boolean admin){Booking booking=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));if(!admin&&!booking.getUserId().equals(userId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");if(List.of("PAID","REFUND_PENDING","REFUND_APPROVED","REFUNDED","COMPLETED").contains(booking.getStatus().toUpperCase()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Paid or completed bookings must use the refund workflow where applicable");if("CANCELLED".equalsIgnoreCase(booking.getStatus()))return booking;String old=booking.getStatus();booking=transitions.transition(booking,userId,"CANCELLED",admin?"Admin cancelled booking":"Customer cancelled booking");audit.record(userId,"BOOKING_CANCELLED","BOOKING",id,old,"CANCELLED");notifications.send(booking.getUserId(),"BOOKING","Booking cancelled","Booking #"+id+" was cancelled","BOOKING",id);return booking;}

  private boolean isAvailable(String type,Long assetId,LocalDate startDate,LocalDate endDate){return !repo.existsByAssetTypeAndAssetIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(type,assetId,BLOCKING,endDate,startDate)&&!availabilityBlocks.existsByAssetTypeAndAssetIdAndStartDateLessThanAndEndDateGreaterThan(type,assetId,endDate,startDate);}
  private void validateDates(LocalDate startDate,LocalDate endDate){if(startDate==null||endDate==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Start and end dates are required");if(startDate.isBefore(LocalDate.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Start date cannot be in the past");if(!endDate.isAfter(startDate))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"End date must be after start date");}
  private String normalizeType(String type){String normalized=type==null?"":type.trim().toUpperCase();if(!List.of("CAR","PROPERTY").contains(normalized))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"assetType must be CAR or PROPERTY");return normalized;}
  private ListingPrice listing(String type,Long id){if("CAR".equals(type)){Car c=cars.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Car not found"));if(!"AVAILABLE".equalsIgnoreCase(c.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Car is not available");return new ListingPrice(c.getDailyRate(),c.getCity());}Property p=props.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Property not found"));if(!"AVAILABLE".equalsIgnoreCase(p.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Property is not available");return new ListingPrice(p.getNightlyRate(),p.getCity());}
  private record ListingPrice(BigDecimal rate,String city){}
}
