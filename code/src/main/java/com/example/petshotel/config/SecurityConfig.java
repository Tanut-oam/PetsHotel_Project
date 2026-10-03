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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,ObjectProvider<UserDetailsService> userDetailsServiceProvider,@Value("${app.remember-me.key:petstay-dev-remember-me-key}") String rememberMeKey) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // เปิดสาธารณะ
                .requestMatchers("/", "/login", "/register", "/error",
                        "/css/**", "/js/**", "/images/**", "/uploads/**",
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/room/**").permitAll()
                .requestMatchers("/rooms/available").permitAll()

                // ADMIN เท่านั้น (ห้อง / โปรโมชั่น / บริการเสริม)
                .requestMatchers("/admin/bookings", "/admin/bookings/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/admin/**", "/dashboard/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/room/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/room/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/room/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/room/**",
                        "/api/promotions/**", "/api/extra-services/**").hasRole("ADMIN")
                        .requestMatchers(
                        HttpMethod.GET,
                        "/rooms",
                        "/rooms/{id:[0-9]+}"
                ).permitAll()
                .requestMatchers("/rooms/**").hasRole("ADMIN")

                // STAFF และ ADMIN
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/bookings/*/payment/confirm",
                    "/api/bookings/*/confirm",
                    "/api/bookings/*/check-in",
                    "/api/bookings/*/check-out"
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
                .successHandler(new RoleBasedLoginSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout.permitAll())
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .exceptionHandling(ex -> ex
                // API ที่ยังไม่ล็อกอินให้ตอบ 401
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    request -> request.getRequestURI().startsWith("/api/")
                )

                // หน้าเว็บที่ยังไม่ล็อกอินให้พาไปหน้า login
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    request -> !request.getRequestURI().startsWith("/api/")
                )
            );

                // จำการล็อกอินไว้ 30 วัน จนกว่าจะกด logout (logout จะลบ cookie remember-me ให้อัตโนมัติ)
        UserDetailsService userDetailsService = userDetailsServiceProvider.getIfAvailable();
        if (userDetailsService != null) {
            http.rememberMe(remember -> remember
                .userDetailsService(userDetailsService)
                .key(rememberMeKey)
                .alwaysRemember(true)
                .tokenValiditySeconds(30 * 24 * 60 * 60)
            );
        }
        return http.build();
    }
}