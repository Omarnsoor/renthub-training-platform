package com.renthub.property;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins="http://localhost:5173")
public class PropertyController {
  private final PropertyRepository repo;
  private final AuthService auth;

  public PropertyController(PropertyRepository repo, AuthService auth) {
    this.repo = repo;
    this.auth = auth;
  }

  @GetMapping
  public List<Property> all() {
    return repo.findAll().stream().filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus())).toList();
  }

  @GetMapping("/mine")
  public List<Property> mine(@RequestHeader(value="Authorization", required=false) String header) {
    User user = auth.require(header);
    auth.requireRole(user, "OWNER", "ADMIN");
    if ("ADMIN".equalsIgnoreCase(user.getRole())) return repo.findAll();
    return repo.findAll().stream().filter(p -> user.getId().equals(p.getOwnerId())).toList();
  }

  @GetMapping("/{id}")
  public Property one(@PathVariable Long id) {
    Property property = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    if (!"AVAILABLE".equalsIgnoreCase(property.getStatus())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
    }
    return property;
  }

  @PostMapping
  public Property create(
      @RequestHeader(value="Authorization", required=false) String header,
      @RequestBody Property input
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
  public Property update(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id,
      @RequestBody Property input
  ) {
    User user = auth.require(header);
    auth.requireRole(user, "OWNER", "ADMIN");
    Property property = managed(user, id);
    validate(input);
    property.setTitle(input.getTitle());
    property.setType(input.getType());
    property.setCity(input.getCity());
    property.setBedrooms(input.getBedrooms());
    property.setBathrooms(input.getBathrooms());
    property.setNightlyRate(input.getNightlyRate());
    property.setDescription(input.getDescription());
    property.setImageUrl(input.getImageUrl());
    if (input.getStatus() != null && !input.getStatus().isBlank()) property.setStatus(input.getStatus().toUpperCase());
    return repo.save(property);
  }

  @DeleteMapping("/{id}")
  public Property archive(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id
  ) {
    User user = auth.require(header);
    auth.requireRole(user, "OWNER", "ADMIN");
    Property property = managed(user, id);
    property.setStatus("INACTIVE");
    return repo.save(property);
  }

  private Property managed(User user, Long id) {
    Property property = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !user.getId().equals(property.getOwnerId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property");
    }
    return property;
  }

  private void validate(Property property) {
    if (property.getTitle() == null || property.getTitle().isBlank()
        || property.getType() == null || property.getType().isBlank()
        || property.getCity() == null || property.getCity().isBlank()
        || property.getBedrooms() == null || property.getBedrooms() < 0
        || property.getBathrooms() == null || property.getBathrooms() < 1
        || property.getNightlyRate() == null || property.getNightlyRate().signum() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete property details and a positive nightly rate are required");
    }
  }
}
