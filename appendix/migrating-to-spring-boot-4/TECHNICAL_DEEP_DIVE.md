# Technical Deep Dive: Appendix - Migrating to Spring Boot 4

## 1. Migration Checklist
1. **JDK Upgrade**: Migrate baseline runtime to **Java 21 LTS** or later.
2. **Jakarta EE 11**: Update imports to `jakarta.*` packages.
3. **Configuration Properties**: Review deprecated properties (e.g. `spring.data.mongodb.*` -> `spring.mongodb.*`).
4. **Security Filter Chains**: Ensure all security configurations use component-based `SecurityFilterChain` beans.
5. **Build Plugins**: Upgrade Gradle to **8.14+ / 9.x** or Maven to **3.9+**.
