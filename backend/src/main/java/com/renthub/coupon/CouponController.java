package com.renthub.coupon;

import com.renthub.auth.AuthService;
import com.renthub.booking.Booking;
import com.renthub.user.User;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins="http://localhost:5173")
public class CouponController {
 private final CouponRepository repo; private final CouponService service; private final AuthService auth;
 public CouponController(CouponRepository repo,CouponService service,AuthService auth){this.repo=repo;this.service=service;this.auth=auth;}
 @PostMapping("/validate") public CouponService.Quote validate(@RequestHeader(value="Authorization",required=false)String h,@RequestBody Map<String,Object>b){User u=auth.require(h);return service.validate(u.getId(),String.valueOf(b.get("code")),new BigDecimal(String.valueOf(b.get("amount"))));}
 @PostMapping("/booking/{bookingId}/apply") public Booking apply(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long bookingId,@RequestBody Map<String,Object>b){User u=auth.require(h);return service.apply(u.getId(),bookingId,String.valueOf(b.get("code")));}
 @GetMapping("/admin") public List<Coupon> admin(@RequestHeader(value="Authorization",required=false)String h){User u=auth.require(h);auth.requireRole(u,"ADMIN");return repo.findAll();}
 @PostMapping("/admin") public Coupon create(@RequestHeader(value="Authorization",required=false)String h,@RequestBody Coupon c){User u=auth.require(h);auth.requireRole(u,"ADMIN");c.setCode(c.getCode().trim().toUpperCase());c.setUsedCount(0);return repo.save(c);}
}
