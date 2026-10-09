package com.renthub.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage,Long>{
 long countByCouponIdAndUserId(Long couponId,Long userId);
 Optional<CouponUsage> findByBookingId(Long bookingId);
}
