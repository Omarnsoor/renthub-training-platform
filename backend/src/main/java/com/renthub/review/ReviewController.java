package com.renthub.review;

import com.renthub.auth.AuthService;
import com.renthub.booking.BookingRepository;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins="http://localhost:5173")
public class ReviewController {
  private final ReviewRepository repo;
  private final AuthService auth;
  private final BookingRepository bookings;

  public ReviewController(ReviewRepository repo, AuthService auth, BookingRepository bookings) {
    this.repo = repo;
    this.auth = auth;
    this.bookings = bookings;
  }

  @GetMapping("/{type}/{assetId}")
  public List<Review> list(@PathVariable String type, @PathVariable Long assetId) {
    return repo.findByAssetTypeAndAssetIdOrderByCreatedAtDesc(type.toUpperCase(), assetId);
  }

  @PostMapping
  public Review save(
      @RequestHeader(value="Authorization", required=false) String header,
      @RequestBody Map<String,Object> body
  ) {
    User user = auth.require(header);
    String type = String.valueOf(body.get("assetType")).trim().toUpperCase();
    Long assetId = Long.valueOf(String.valueOf(body.get("assetId")));
    int rating = Integer.parseInt(String.valueOf(body.get("rating")));

    if (!List.of("CAR", "PROPERTY").contains(type)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetType must be CAR or PROPERTY");
    }
    if (rating < 1 || rating > 5) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
    }
    if (!bookings.existsByUserIdAndAssetTypeAndAssetIdAndStatusIn(
        user.getId(), type, assetId, List.of("PAID", "COMPLETED"))) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "You can review this listing after a paid booking");
    }

    Review review = repo.findByUserIdAndAssetTypeAndAssetId(user.getId(), type, assetId)
        .orElseGet(Review::new);
    review.setUserId(user.getId());
    review.setAssetType(type);
    review.setAssetId(assetId);
    review.setRating(rating);
    String comment = body.get("comment") == null ? "" : String.valueOf(body.get("comment")).trim();
    if (comment.length() > 1000) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review comment is too long");
    }
    review.setComment(comment);
    return repo.save(review);
  }
}
