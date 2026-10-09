package com.renthub.booking;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins="http://localhost:5173")
public class BookingLifecycleController {
 private final BookingLifecycleService service; private final AuthService auth;
 public BookingLifecycleController(BookingLifecycleService service,AuthService auth){this.service=service;this.auth=auth;}
 @PatchMapping("/{id}/check-in") public Booking checkIn(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){return service.checkIn(auth.require(h),id);}
 @PatchMapping("/{id}/complete") public Booking complete(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@RequestBody(required=false)Map<String,Object>b){User u=auth.require(h);boolean force=b!=null&&Boolean.parseBoolean(String.valueOf(b.getOrDefault("force","false")));return service.complete(u,id,force);}
 @GetMapping("/{id}/history") public List<BookingHistory> history(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){return service.history(auth.require(h),id);}
}
