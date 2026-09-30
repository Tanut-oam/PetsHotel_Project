package com.example.petshotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
                .requestMatchers(HttpMethod.GET, "/api/room/**").permitAll()
                .requestMatchers("/rooms/available").permitAll()

                // ADMIN เท่านั้น (ห้อง / โปรโมชั่น / บริการเสริม)
                .requestMatchers("/admin/**", "/dashboard/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/room/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/room/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/room/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                .requestMatchers("/rooms/**").hasRole("ADMIN")

                // STAFF และ ADMIN
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/bookings//payment/confirm",
                    "/api/bookings//confirm",
                    "/api/bookings//check-in",
                    "/api/bookings//check-out"
                ).hasAnyRole("STAFF", "ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/bookings",
                    "/api/bookings/"
                ).hasAnyRole("STAFF", "ADMIN")

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/care-reports/**"
                ).hasAnyRole("STAFF", "ADMIN")

                // ที่เหลือต้องล็อกอิน (ตรวจความเป็นเจ้าของใน Service ภายหลัง)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", false)
                .permitAll()
            )
            .logout(logout -> logout.permitAll())
            .exceptionHandling(ex -> ex
                // /api/** ตอบ 401 แทนการ redirect ไปหน้า login
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    request -> request.getRequestURI().startsWith("/api/"))
            );

        return http.build();
    }
}