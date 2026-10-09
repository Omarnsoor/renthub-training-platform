package com.renthub.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
  @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(csrf->csrf.disable()).cors(Customizer.withDefaults())
      .authorizeHttpRequests(a->a.requestMatchers("/api/**").permitAll().anyRequest().permitAll()).build();
  }
}
