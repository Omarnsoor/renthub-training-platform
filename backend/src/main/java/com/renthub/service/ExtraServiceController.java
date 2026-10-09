package com.renthub.service;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins="http://localhost:5173")
public class ExtraServiceController {
  private final ExtraServiceRepository repo;
  private final AuthService auth;

  public ExtraServiceController(ExtraServiceRepository repo, AuthService auth) {
    this.repo = repo;
    this.auth = auth;
  }

  @GetMapping
  public List<ExtraService> all() {
    return repo.findAll().stream().filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus())).toList();
  }

  @GetMapping("/all")
  public List<ExtraService> allForAdmin(@RequestHeader(value="Authorization", required=false) String header) {
    User user = auth.require(header);
    auth.requireRole(user, "ADMIN");
    return repo.findAll();
  }

  @PostMapping
  public ExtraService create(
      @RequestHeader(value="Authorization", required=false) String header,
      @RequestBody ExtraService service
  ) {
    User user = auth.require(header);
    auth.requireRole(user, "ADMIN");
    validate(service);
    service.setId(null);
    service.setStatus(service.getStatus() == null || service.getStatus().isBlank() ? "ACTIVE" : service.getStatus().toUpperCase());
    return repo.save(service);
  }

  @PatchMapping("/{id}")
  public ExtraService update(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id,
      @RequestBody ExtraService input
  ) {
    User user = auth.require(header);
    auth.requireRole(user, "ADMIN");
    ExtraService service = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    validate(input);
    service.setName(input.getName());
    service.setCategory(input.getCategory());
    service.setPrice(input.getPrice());
    service.setDescription(input.getDescription());
    service.setImageUrl(input.getImageUrl());
    if (input.getStatus() != null && !input.getStatus().isBlank()) service.setStatus(input.getStatus().toUpperCase());
    return repo.save(service);
  }

  @DeleteMapping("/{id}")
  public ExtraService archive(
      @RequestHeader(value="Authorization", required=false) String header,
      @PathVariable Long id
  ) {
    User user = auth.require(header);
    auth.requireRole(user, "ADMIN");
    ExtraService service = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    service.setStatus("INACTIVE");
    return repo.save(service);
  }

  private void validate(ExtraService service) {
    if (service.getName() == null || service.getName().isBlank()
        || service.getCategory() == null || service.getCategory().isBlank()
        || service.getPrice() == null || service.getPrice().signum() < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service name, category and non-negative price are required");
    }
  }
}
