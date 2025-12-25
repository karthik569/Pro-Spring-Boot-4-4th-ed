package com.apress.crm.customer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(CustomerRepository repository) {
        return username -> repository.findByEmail(username)
                .map(customer -> User.withUsername(customer.getEmail())
                        .password(customer.getPassword()) // Must be encoded (e.g., BCrypt)
                        .authorities("ROLE_USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/customers/**").hasAuthority("SCOPE_read")
                .requestMatchers(HttpMethod.POST, "/api/v1/customers").hasRole("ADMIN")
                .requestMatchers("/api/v1/public/**", "/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults());

        return http.build();
    }

    /*
    @Profile("mtls")
    @Bean
    public SecurityFilterChain x509FilterChain(HttpSecurity http) throws Exception {
        return http
            .x509(Customizer.withDefaults())
            .authorizeHttpRequests(ex -> ex.anyRequest().authenticated())
            .csrf(Customizer.withDefaults())
            .build();
    }
    */

    /*
    @Profile("ott")
    @Bean
    public SecurityFilterChain ottFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(f -> f.sameOrigin()))
            .oneTimeTokenLogin(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
    */

    /*
    @Bean
    public SecurityFilterChain exploitFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("https://trusted-frontend.com"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                return config;
            }));
        return http.build();
    }
    */

    /*
    @Profile("mfa")
    @Bean
    public SecurityFilterChain mfaFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/admin/**").hasRole("MFA_AUTHENTICATED")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // If authenticated but missing MFA role, redirect to verify
                    response.sendRedirect("/mfa-verify");
                })
            )
            .formLogin(Customizer.withDefaults());
        return http.build();
    }
    */
}