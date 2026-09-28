package com.example.petshotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // เปิดสาธารณะ
                .requestMatchers("/", "/login", "/register",
                        "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                .requestMatchers("/api/availability/**").permitAll()

                // ADMIN เท่านั้น
                .requestMatchers("/admin/**", "/dashboard/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/rooms/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/rooms/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/rooms/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")

                // STAFF และ ADMIN
                .requestMatchers("/api/bookings/*/payment/confirm").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/care-reports/**").hasAnyRole("STAFF", "ADMIN")

                // ที่เหลือต้องล็อกอิน (ตรวจความเป็นเจ้าของใน Service ภายหลัง)
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())   // หน้า login สำเร็จรูป ใช้ทดสอบก่อน
            .logout(Customizer.withDefaults())
            .exceptionHandling(ex -> ex
                // /api/** ตอบ 401 แทนการ redirect ไปหน้า login
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    request -> request.getRequestURI().startsWith("/api/"))
            );

        return http.build();
    }
}
