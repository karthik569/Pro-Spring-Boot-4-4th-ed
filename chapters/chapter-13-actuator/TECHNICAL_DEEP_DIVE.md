# Technical Deep Dive: Chapter 13 - Production Observability, Metrics & Actuator

## 1. Architectural Overview
Chapter 13 details production readiness through **Spring Boot Actuator**, **Micrometer Metrics**, **Prometheus**, and **OpenTelemetry Distributed Tracing**.

```mermaid
graph TD
    APP[Spring Boot Application] --> ACT[/actuator/health, /actuator/metrics]
    APP --> METER[Micrometer MeterRegistry]
    METER --> PROM[Prometheus Scrape Endpoint]
    APP --> TRACE[OpenTelemetry Tracing Context]
    TRACE --> ZIPKIN[Distributed Trace Collector]
```

## 2. Observability Features
- **Custom Health Indicators**: Implements `HealthIndicator` checking downstream database latency and remote service availability.
- **Micrometer Counters & Timers**: Custom metrics measuring business events (e.g. `customer.registered.total`).
- **Observation API**: Unifies tracing and metrics collection via `@Observed` and `ObservationRegistry`.

## 3. Build & Verification
```bash
cd chapters/chapter-13-actuator/customer && mvn test
cd ../management && bash ./gradlew test
```
