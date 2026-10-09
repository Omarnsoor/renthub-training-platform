package com.renthub.integration.payment;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalPaymentGateway implements PaymentGateway {
 @Override public GatewayResult charge(Long bookingId,BigDecimal amount,String method,String scenario){
  String s=scenario==null?"SUCCESS":scenario.toUpperCase();
  if("DECLINE".equals(s))return new GatewayResult(false,"LOCALPAY",null,"CARD_DECLINED","Mock card decline");
  if("TIMEOUT".equals(s))return new GatewayResult(false,"LOCALPAY",null,"PROVIDER_TIMEOUT","Mock provider timeout");
  if("INSUFFICIENT_FUNDS".equals(s))return new GatewayResult(false,"LOCALPAY",null,"INSUFFICIENT_FUNDS","Mock insufficient funds");
  return new GatewayResult(true,"LOCALPAY","LP-"+UUID.randomUUID().toString().substring(0,10).toUpperCase(),null,"Captured");
 }
}
