package com.renthub.integration.payment;

import java.math.BigDecimal;

public interface PaymentGateway {
  GatewayResult charge(Long bookingId,BigDecimal amount,String method,String scenario);
  record GatewayResult(boolean success,String provider,String reference,String failureCode,String message){}
}
