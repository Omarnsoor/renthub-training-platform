package com.renthub.payment;

import com.renthub.auth.AuthService;
import com.renthub.user.User;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins="http://localhost:5173")
public class PaymentController {
  private final PaymentService service; private final AuthService auth;
  public PaymentController(PaymentService service,AuthService auth){this.service=service;this.auth=auth;}

  @GetMapping("/booking/{id}")
  public Payment receipt(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){return service.receipt(auth.require(h),id);}

  @GetMapping("/booking/{id}/attempts")
  public List<PaymentAttempt> attempts(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){return service.attempts(auth.require(h),id);}

  @PostMapping("/booking/{id}")
  public Payment pay(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id,@RequestBody(required=false)Map<String,Object> body){
    User user=auth.require(h);String method=body==null?"CARD":String.valueOf(body.getOrDefault("method","CARD"));String scenario=body==null?"SUCCESS":String.valueOf(body.getOrDefault("scenario","SUCCESS"));return service.pay(user,id,method,scenario);
  }
}
