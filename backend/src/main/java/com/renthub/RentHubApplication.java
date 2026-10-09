package com.renthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RentHubApplication {
  public static void main(String[] args) { SpringApplication.run(RentHubApplication.class, args); }
}
