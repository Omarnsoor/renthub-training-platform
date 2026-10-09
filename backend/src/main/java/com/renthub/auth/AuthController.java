package com.renthub.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins="http://localhost:5173")
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  public record RegisterRequest(@NotBlank String fullName, @Email @NotBlank String email, @Size(min=6) String password) {}
  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
  public record ProfileRequest(@NotBlank String fullName, @Email @NotBlank String email) {}
  public record PasswordRequest(@NotBlank String currentPassword, @Size(min=6) String newPassword) {}

  @PostMapping("/register")
  public AuthService.AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return auth.register(request.fullName(), request.email(), request.password());
  }

  @PostMapping("/login")
  public AuthService.AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return auth.login(request.email(), request.password());
  }

  @GetMapping("/me")
  public AuthService.UserView me(@RequestHeader(value="Authorization",required=false) String header) {
    return auth.me(header);
  }

  @PatchMapping("/me")
  public AuthService.UserView updateProfile(
      @RequestHeader(value="Authorization",required=false) String header,
      @Valid @RequestBody ProfileRequest request
  ) {
    return auth.updateProfile(header, request.fullName(), request.email());
  }

  @PatchMapping("/password")
  public void changePassword(
      @RequestHeader(value="Authorization",required=false) String header,
      @Valid @RequestBody PasswordRequest request
  ) {
    auth.changePassword(header, request.currentPassword(), request.newPassword());
  }

  @PostMapping("/logout")
  public void logout(@RequestHeader(value="Authorization",required=false) String header) {
    auth.logout(header);
  }
}
