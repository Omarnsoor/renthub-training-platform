package com.renthub.coupon;

import com.renthub.audit.AuditService;
import com.renthub.booking.Booking;
import com.renthub.booking.BookingRepository;
import com.renthub.notification.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.LocalDateTime;

@Service
public class CouponService {
  private final CouponRepository coupons; private final CouponUsageRepository usages; private final BookingRepository bookings; private final AuditService audit; private final NotificationService notifications;
  public CouponService(CouponRepository coupons,CouponUsageRepository usages,BookingRepository bookings,AuditService audit,NotificationService notifications){this.coupons=coupons;this.usages=usages;this.bookings=bookings;this.audit=audit;this.notifications=notifications;}

  public record Quote(String code,BigDecimal originalAmount,BigDecimal discount,BigDecimal finalAmount){}

  public Quote validate(Long userId,String code,BigDecimal amount){Coupon c=active(code);BigDecimal discount=discount(c,userId,amount);return new Quote(c.getCode(),amount,discount,amount.subtract(discount));}

  @Transactional
  public Booking apply(Long userId,Long bookingId,String code){
    Booking b=bookings.findById(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));
    if(!b.getUserId().equals(userId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your booking");
    if(!"PENDING".equalsIgnoreCase(b.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Coupon can only be applied before payment");
    if(b.getCouponCode()!=null&&!b.getCouponCode().isBlank())throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking already has a coupon");
    Coupon c=active(code);BigDecimal before=b.getTotalAmount();BigDecimal d=discount(c,userId,before);
    CouponUsage usage=new CouponUsage();usage.setCouponId(c.getId());usage.setUserId(userId);usage.setBookingId(bookingId);usage.setDiscountAmount(d);usages.save(usage);
    c.setUsedCount((c.getUsedCount()==null?0:c.getUsedCount())+1);coupons.save(c);
    b.setCouponCode(c.getCode());b.setDiscountAmount(d);b.setTotalAmount(before.subtract(d));bookings.save(b);
    audit.record(userId,"COUPON_APPLIED","BOOKING",bookingId,before,b.getTotalAmount());
    notifications.send(userId,"BOOKING","Coupon applied","Coupon "+c.getCode()+" saved "+d+" on booking #"+bookingId,"BOOKING",bookingId);
    return b;
  }

  private Coupon active(String code){
    Coupon c=coupons.findByCodeIgnoreCase(code==null?"":code.trim()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Coupon not found"));
    LocalDateTime now=LocalDateTime.now();
    if(!"ACTIVE".equalsIgnoreCase(c.getStatus())||now.isBefore(c.getStartsAt())||now.isAfter(c.getExpiresAt()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Coupon is not active");
    if(c.getGlobalUsageLimit()!=null&&c.getUsedCount()!=null&&c.getUsedCount()>=c.getGlobalUsageLimit())throw new ResponseStatusException(HttpStatus.CONFLICT,"Coupon usage limit reached");
    return c;
  }

  private BigDecimal discount(Coupon c,Long userId,BigDecimal amount){
    if(amount.compareTo(c.getMinAmount()==null?BigDecimal.ZERO:c.getMinAmount())<0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Booking does not meet coupon minimum amount");
    if(c.getPerUserLimit()!=null&&usages.countByCouponIdAndUserId(c.getId(),userId)>=c.getPerUserLimit())throw new ResponseStatusException(HttpStatus.CONFLICT,"You reached the coupon usage limit");
    BigDecimal d="PERCENT".equalsIgnoreCase(c.getDiscountType())?amount.multiply(c.getDiscountValue()).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP):c.getDiscountValue();
    if(c.getMaxDiscount()!=null&&d.compareTo(c.getMaxDiscount())>0)d=c.getMaxDiscount();
    if(d.compareTo(amount)>0)d=amount;
    return d.setScale(2,RoundingMode.HALF_UP);
  }
}
