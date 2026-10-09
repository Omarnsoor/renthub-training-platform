package com.renthub.car;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin(origins="http://localhost:5173")
public class CarController {
  private final CarRepository repo;
  private final AuthService auth;

  public CarController(CarRepository repo, AuthService auth) {
    this.repo = repo;
    this.auth = auth;
  }

  @GetMapping
  public List<Car> all() {
    return repo.findAll().stream().filter(c -> !"INACTIVE".equalsIgnoreCase(c.getStatus())).toList();
  }

  @GetMapping("/mine")
  public List<Car> mine(@RequestHeader(value="Authorization", required=false) String header) {
    User user = auth.require(header);
    auth.requireRole(user, "OWNER", "ADMIN");
    if ("ADMIN".equalsIgnoreCase(user.getRole())) return repo.findAll();
    return repo.findAll().stream().filter(c -> user.getId().equals(c.getOwnerId())).toList();
  }

  @GetMapping("/{id}")
  public Car one(@PathVariable Long id) {
    Car car = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
    if ("INACTIVE".equalsIgnoreCase(car.getStatus())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found");
    }
    return car;
  }

  @PostMapping
  public Car create(
      @RequestHeader(value="Authorization", required=false) String header,
      @RequestBody Car input
  ) {
    User user = auth.require(header);
    auth.requireRole(user, "OWNER", "ADMIN");
    validate(input);
    input.setId(null);
    input.setOwnerId(user.getId());
    input.setStatus(input.getStatus() == null || input.getStatus().isBlank() ? "AVAILABLE" : input.getStatus().toUpperCase());
    return repo.save(input);
  }

  @PatchMapping("/{id}")
  public Car update(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id,
      @RequestBody Car input
  ) {
    User user = auth.require(header);
    auth.requireRole(user, "OWNER", "ADMIN");
    Car car = managed(user, id);
    validate(input);
    car.setMake(input.getMake());
    car.setModel(input.getModel());
    car.setModelYear(input.getModelYear());
    car.setTransmission(input.getTransmission());
    car.setSeats(input.getSeats());
    car.setDailyRate(input.getDailyRate());
    car.setCity(input.getCity());
    car.setImageUrl(input.getImageUrl());
    if (input.getStatus() != null && !input.getStatus().isBlank()) car.setStatus(input.getStatus().toUpperCase());
    return repo.save(car);
  }

  @DeleteMapping("/{id}")
  public Car archive(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id
  ) {
    User user = auth.require(header);
    auth.requireRole(user, "OWNER", "ADMIN");
    Car car = managed(user, id);
    car.setStatus("INACTIVE");
    return repo.save(car);
  }

  private Car managed(User user, Long id) {
    Car car = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
    if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !user.getId().equals(car.getOwnerId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this car");
    }
    return car;
  }

  private void validate(Car car) {
    if (car.getMake() == null || car.getMake().isBlank() || car.getModel() == null || car.getModel().isBlank()
        || car.getModelYear() == null || car.getSeats() == null || car.getSeats() < 1
        || car.getDailyRate() == null || car.getDailyRate().signum() <= 0
        || car.getCity() == null || car.getCity().isBlank()
        || car.getTransmission() == null || car.getTransmission().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete car details and a positive daily rate are required");
    }
  }
}
