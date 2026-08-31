# Technical Deep Dive: Chapter 11 - Spring Security 7 & OAuth2 Architecture

## 1. Architectural Overview
Chapter 11 implements zero-trust modern application security using **Spring Security 7**, component-based `SecurityFilterChain` configurations, stateless JWT token authentication, and role-based method security.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Filter as BearerTokenAuthenticationFilter
    participant Decoder as JwtDecoder
    participant Security as SecurityContextHolder
    participant Endpoint as Protected RestController

    Client->>Filter: Request + Bearer JWT
    Filter->>Decoder: Validate Signature & Claims
    Decoder-->>Filter: Authenticated JwtAuthenticationToken
    Filter->>Security: Set SecurityContext
    Filter->>Endpoint: Proceed (Evaluates @PreAuthorize)
    Endpoint-->>Client: 200 OK Response
```

## 2. Key Implementations
- **Stateless Filter Chain**:
  ```java
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      return http
          .csrf(CsrfConfigurer::disable)
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/public/**").permitAll()
              .requestMatchers("/api/admin/**").hasRole("ADMIN")
              .anyRequest().authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
          .build();
  }
  ```
- **Method Security**: Enables `@EnableMethodSecurity` and enforces fine-grained `@PreAuthorize("hasRole('ADMIN')")`.

## 3. Build & Verification
```bash
cd chapters/chapter-11-security/customer && mvn test
cd ../management && bash ./gradlew test
```
