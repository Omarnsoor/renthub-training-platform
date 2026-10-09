package com.renthub.availability;

import com.renthub.booking.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AvailabilityPolicyService {
  private static final List<String> BLOCKING_BOOKING_STATUSES = List.of("PENDING","CONFIRMED","PAID","REFUND_PENDING","REFUND_APPROVED");

  private final BookingRepository bookings;
  private final AvailabilityBlockRepository blocks;

  public AvailabilityPolicyService(BookingRepository bookings, AvailabilityBlockRepository blocks) {
    this.bookings = bookings;
    this.blocks = blocks;
  }

  public void assertCanBlock(String rawType, Long assetId, LocalDate startDate, LocalDate endDate) {
    String type = normalizeType(rawType);
    if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid start and end dates are required");
    }
    boolean overlapsBooking = bookings.existsByAssetTypeAndAssetIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
        type, assetId, BLOCKING_BOOKING_STATUSES, endDate, startDate);
    if (overlapsBooking) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "The asset has an active booking in the requested block period");
    }
  }

  public AvailabilityBlock create(String rawType, Long assetId, LocalDate startDate, LocalDate endDate,
                                  String reason, String blockType, Long createdBy) {
    String type = normalizeType(rawType);
    assertCanBlock(type, assetId, startDate, endDate);
    AvailabilityBlock block = new AvailabilityBlock();
    block.setAssetType(type);
    block.setAssetId(assetId);
    block.setStartDate(startDate);
    block.setEndDate(endDate);
    block.setReason(reason);
    block.setBlockType(blockType == null || blockType.isBlank() ? "OWNER_BLOCK" : blockType.toUpperCase());
    block.setCreatedBy(createdBy);
    return blocks.save(block);
  }

  public void remove(Long blockId) {
    if (blockId == null) return;
    blocks.findById(blockId).ifPresent(blocks::delete);
  }

  private String normalizeType(String rawType) {
    String type = rawType == null ? "" : rawType.trim().toUpperCase();
    if (!List.of("CAR","PROPERTY").contains(type)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported asset type");
    }
    return type;
  }
}
