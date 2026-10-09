package com.renthub.favorite;

import com.renthub.auth.AuthService;
import com.renthub.car.Car;
import com.renthub.car.CarRepository;
import com.renthub.property.Property;
import com.renthub.property.PropertyRepository;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins="http://localhost:5173")
public class FavoriteController {
  private final FavoriteRepository repo;
  private final AuthService auth;
  private final CarRepository cars;
  private final PropertyRepository properties;

  public FavoriteController(FavoriteRepository repo, AuthService auth, CarRepository cars, PropertyRepository properties) {
    this.repo = repo;
    this.auth = auth;
    this.cars = cars;
    this.properties = properties;
  }

  @GetMapping
  public List<Favorite> all(@RequestHeader(value="Authorization", required=false) String header) {
    return repo.findByUserIdOrderByIdDesc(auth.require(header).getId());
  }

  @PostMapping
  public Favorite add(
      @RequestHeader(value="Authorization", required=false) String header,
      @RequestBody Map<String,Object> body
  ) {
    User user = auth.require(header);
    String type = String.valueOf(body.get("assetType")).trim().toUpperCase();
    Long assetId = Long.valueOf(String.valueOf(body.get("assetId")));
    validateAsset(type, assetId);
    return repo.findByUserIdAndAssetTypeAndAssetId(user.getId(), type, assetId)
        .orElseGet(() -> {
          Favorite favorite = new Favorite();
          favorite.setUserId(user.getId());
          favorite.setAssetType(type);
          favorite.setAssetId(assetId);
          return repo.save(favorite);
        });
  }

  @DeleteMapping("/{type}/{assetId}")
  public void remove(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable String type,
      @PathVariable Long assetId
  ) {
    User user = auth.require(header);
    repo.findByUserIdAndAssetTypeAndAssetId(user.getId(), type.toUpperCase(), assetId).ifPresent(repo::delete);
  }

  private void validateAsset(String type, Long assetId) {
    if ("CAR".equals(type)) {
      Car car = cars.findById(assetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
      if (!"AVAILABLE".equalsIgnoreCase(car.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Car is not available");
      return;
    }
    if ("PROPERTY".equals(type)) {
      Property property = properties.findById(assetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
      if (!"AVAILABLE".equalsIgnoreCase(property.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Property is not available");
      return;
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetType must be CAR or PROPERTY");
  }
}
