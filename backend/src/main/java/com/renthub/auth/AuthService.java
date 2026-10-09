package com.renthub.auth;

import com.renthub.user.User;
import com.renthub.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
  private final UserRepository users;
  private final AuthSessionRepository sessions;
  private final PasswordEncoder encoder;

  public AuthService(UserRepository users, AuthSessionRepository sessions, PasswordEncoder encoder) {
    this.users = users;
    this.sessions = sessions;
    this.encoder = encoder;
  }

  public record UserView(Long id, String fullName, String email, String role, String status) {}
  public record AuthResponse(String token, UserView user) {}

  private UserView view(User user) {
    return new UserView(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getStatus());
  }

  @Transactional
  public AuthResponse register(String name, String email, String password) {
    if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.length() < 6) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, valid email and password of at least 6 characters are required");
    }
    if (users.findByEmailIgnoreCase(email.trim()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
    }
    User user = new User();
    user.setFullName(name.trim());
    user.setEmail(email.trim().toLowerCase());
    user.setPasswordHash(encoder.encode(password));
    user.setRole("CUSTOMER");
    user = users.save(user);
    return issue(user);
  }

  @Transactional
  public AuthResponse login(String email, String password) {
    User user = users.findByEmailIgnoreCase(email == null ? "" : email.trim())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
    if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
    }
    if ("RESET_REQUIRED".equals(user.getPasswordHash())) {
      String expected = "ADMIN".equalsIgnoreCase(user.getRole()) ? "Admin123!" : "Owner123!";
      if (!expected.equals(password)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
      }
      user.setPasswordHash(encoder.encode(password));
      users.save(user);
    } else if (!encoder.matches(password == null ? "" : password, user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
    return issue(user);
  }

  @Transactional
  public UserView updateProfile(String header, String fullName, String email) {
    User user = require(header);
    String nextName = fullName == null ? "" : fullName.trim();
    String nextEmail = email == null ? "" : email.trim().toLowerCase();
    if (nextName.isBlank() || nextEmail.isBlank() || !nextEmail.contains("@")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name and a valid email are required");
    }
    users.findByEmailIgnoreCase(nextEmail).ifPresent(existing -> {
      if (!existing.getId().equals(user.getId())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
      }
    });
    user.setFullName(nextName);
    user.setEmail(nextEmail);
    return view(users.save(user));
  }

  @Transactional
  public void changePassword(String header, String currentPassword, String newPassword) {
    User user = require(header);
    if (newPassword == null || newPassword.length() < 6) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 6 characters");
    }
    if (!encoder.matches(currentPassword == null ? "" : currentPassword, user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
    }
    user.setPasswordHash(encoder.encode(newPassword));
    users.save(user);
  }

  private AuthResponse issue(User user) {
    AuthSession session = new AuthSession();
    session.setToken(UUID.randomUUID().toString() + UUID.randomUUID().toString());
    session.setUserId(user.getId());
    session.setExpiresAt(LocalDateTime.now().plusDays(7));
    sessions.save(session);
    return new AuthResponse(session.getToken(), view(user));
  }

  public User require(String header) {
    if (header == null || !header.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in required");
    }
    String token = header.substring(7).trim();
    AuthSession session = sessions.findByToken(token)
        .filter(item -> item.getExpiresAt().isAfter(LocalDateTime.now()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired. Sign in again"));
    return users.findById(session.getUserId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
  }

  public UserView me(String header) {
    return view(require(header));
  }

  @Transactional
  public void logout(String header) {
    if (header != null && header.startsWith("Bearer ")) {
      sessions.deleteByToken(header.substring(7).trim());
    }
  }

  public void requireRole(User user, String... roles) {
    for (String role : roles) {
      if (role.equalsIgnoreCase(user.getRole())) return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission for this action");
  }
}
