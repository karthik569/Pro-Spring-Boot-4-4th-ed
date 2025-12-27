# Management CRM - Native Observability with OpenTelemetry

This project demonstrates that even complex, reactive Spring Boot applications with OpenTelemetry tracing can be compiled into native executables using GraalVM. This proves the maturity of Spring Boot 4's native support.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Building the Application](#building-the-application)
- [Advanced AOT Features](#advanced-aot-features)
  - [CRaC Support](#4-crac-coordinated-restore-at-checkpoint-support)
- [Running with Observability](#running-with-observability)
- [Verifying Native Observability](#verifying-native-observability)
- [Testing](#testing)
- [Container Images](#container-images)
- [Performance Benchmarks](#performance-benchmarks)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### 1. Install GraalVM

```bash
# Install SDKMAN if you haven't already
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Liberica NIK (Native Image Kit) for Java 21
sdk install java 23.1.2.r21-nik

# Set it as the default
sdk use java 23.1.2.r21-nik

# Verify
java -version
native-image --version
```

### 2. Docker and Docker Compose

You'll need Docker to run the observability infrastructure (Jaeger, Prometheus, etc.).

### 3. System Requirements

- **Memory**: 8GB minimum (16GB recommended)
- **Disk Space**: ~3GB for build artifacts and Docker images
- **Time**: Native compilation takes 3-6 minutes

## Building the Application

### Standard JVM Build

```bash
# Build the JAR
./gradlew clean build

# The JAR will be in build/libs/management-0.0.1-SNAPSHOT.jar
```

### Native Image Build

```bash
# Build the native executable (this will take several minutes)
./gradlew nativeCompile

# The native executable will be in build/native/nativeCompile/management
```

**Build Options:**

```bash
# Build with verbose output
./gradlew nativeCompile --info

# Clean build
./gradlew clean nativeCompile
```

## Advanced AOT Features

This project demonstrates advanced AOT processing for reactive Spring Boot applications:

### 1. Comprehensive Runtime Hints (`ManagementRuntimeHints`)

Location: `management/src/main/java/com/apress/crm/management/aot/ManagementRuntimeHints.java`

Provides comprehensive hints for:
- **Domain Models**: Registers all R2DBC entities (Customer, Address, Company, Communication)
- **DTOs**: Registers data transfer objects for JSON serialization
- **Resources**: Configuration files, database migrations, GraphQL schemas
- **Serialization**: All domain classes for reactive serialization

```java
// Register all domain model classes for R2DBC and JSON
Class<?>[] domainClasses = {
    Customer.class, Address.class, Company.class, Communication.class
};

for (Class<?> clazz : domainClasses) {
    hints.reflection().registerType(
        TypeReference.of(clazz),
        hint -> hint.withMembers(
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS
        )
    );
}
```

### 2. Advanced Bean Registration (`ManagementBeanRegistrar`)

Location: `management/src/main/java/com/apress/crm/management/aot/ManagementBeanRegistrar.java`

Demonstrates cloud-aware bean registration:
- **Kubernetes Detection**: Registers K8s-specific beans when running on Kubernetes
- **Cloud Foundry Support**: Conditional beans for Cloud Foundry environments
- **Reactive Beans**: Registers reactive WebClient configurations
- **Observability**: Custom observation filters for OpenTelemetry

```java
@Import(ManagementBeanRegistrar.class)  // Enabled in ManagementConfiguration

// Detects cloud platform at startup
if (isRunningOnKubernetes()) {
    registerKubernetesBeans(registry);
}
```

### 3. AOT Factories Registration

Location: `management/src/main/resources/META-INF/spring/aot.factories`

Registers runtime hints for the reactive application:
```properties
org.springframework.aot.hint.RuntimeHintsRegistrar=\
  com.apress.crm.management.aot.ManagementRuntimeHints
```

**Key Insight**: These features prove that complex reactive applications with R2DBC, WebFlux, and OpenTelemetry work seamlessly in native mode with proper AOT hints!

### 4. CRaC (Coordinated Restore at Checkpoint) Support

Location: `management/src/main/java/com/apress/crm/management/crac/`

This project includes a demonstration of **CRaC (Coordinated Restore at Checkpoint)**, an OpenJDK project that allows taking snapshots of running JVM applications and restoring them later with near-instant startup times while preserving JIT optimizations.

**Key Classes:**
- `CracManagedConnection`: Demonstrates resource management during checkpoint/restore lifecycle
- `NetworkConnection`: Simulates a network connection that must be closed before checkpoint

```java
@Component
@ConditionalOnProperty(name = "management.crac.enabled", havingValue = "true")
public class CracManagedConnection implements Resource {

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) {
        // Close connections before checkpoint
        closeConnection();
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) {
        // Reopen connections after restore
        openConnection();
    }
}
```

**Why CRaC?**
- **Instant Startup**: Restore a warmed-up JVM in milliseconds
- **JIT Preserved**: Keep all JIT optimizations from the original run
- **Resource Management**: Properly handle file handles, sockets, etc.

**How to Use:**

1. **Enable CRaC** in `application.properties`:
   ```properties
   management.crac.enabled=true
   ```

2. **Requires CRaC-enabled JDK** (e.g., Azul Zulu with CRaC):
   ```bash
   sdk install java 21.0.1-zulu-crac
   sdk use java 21.0.1-zulu-crac
   ```

3. **Run and take a checkpoint**:
   ```bash
   # Start the application
   java -XX:CRaCCheckpointTo=./checkpoint -jar build/libs/management-0.0.1-SNAPSHOT.jar

   # In another terminal, trigger checkpoint
   jcmd $(pgrep -f management) JDK.checkpoint
   ```

4. **Restore from checkpoint**:
   ```bash
   java -XX:CRaCRestoreFrom=./checkpoint
   ```

**Note**: CRaC is **disabled by default** (`management.crac.enabled=false`) since it requires a special JDK. The library provides no-op stubs when running on a standard JDK, so the code compiles and runs normally without CRaC support.

**Comparison: CRaC vs Native**

| Feature | CRaC | Native (GraalVM) |
|---------|------|------------------|
| Startup | Milliseconds (restore) | Milliseconds (cold) |
| JIT Optimizations | ✅ Preserved | ❌ AOT only |
| Peak Performance | ✅ Excellent | Good |
| Build Time | Fast | Slow (minutes) |
| Portability | Requires CRaC JDK | Standard executable |
| Resource Mgmt | Manual (Resource API) | Automatic |

## Running with Observability

### 1. Start the Observability Infrastructure

The application exports telemetry via OpenTelemetry. You need to start Jaeger and Prometheus:

```bash
# Start Jaeger (for distributed tracing)
docker run -d --name jaeger \
  -p 16686:16686 \
  -p 4318:4318 \
  jaegertracing/all-in-one:latest

# Verify Jaeger is running
curl http://localhost:16686
```

**Alternative**: Use docker-compose if available:
```bash
docker-compose up -d
```

### 2. Start CockroachDB

```bash
docker run -d --name crdb \
  -p 26257:26257 \
  cockroachdb/cockroach:latest start-single-node --insecure
```

### 3. Run the Application

**JVM Version:**
```bash
java -jar build/libs/management-0.0.1-SNAPSHOT.jar
```

**Native Version:**
```bash
./build/native/nativeCompile/management
```

## Verifying Native Observability

This is the critical test that proves native compilation works with complex observability features.

### 1. Generate Some Traffic

```bash
# Health check
curl http://localhost:8080/actuator/health

# Create test data (if endpoints are available)
curl -X POST http://localhost:8080/api/management/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Customer"}'

# Get management data
curl http://localhost:8080/api/management/reports
```

### 2. View Distributed Traces in Jaeger

1. Open Jaeger UI: http://localhost:16686
2. Select "management" from the Service dropdown
3. Click "Find Traces"

**You should see:**
- Trace spans for your HTTP requests
- Custom spans from `management.report` observations
- Region tags (`region=US-East`) added by `RegionObservationHandler`
- Full distributed trace context propagation

**This proves** that OpenTelemetry tracing works seamlessly in your native executable!

### 3. Check Prometheus Metrics

```bash
# View all metrics
curl http://localhost:8080/actuator/prometheus

# Filter for custom metrics
curl http://localhost:8080/actuator/prometheus | grep management
```

### 4. Test Custom Actuator Endpoint

```bash
# Read cache information
curl http://localhost:8080/actuator/managementControl

# Clear cache
curl -X DELETE http://localhost:8080/actuator/managementControl
```

## Testing

### Standard Tests

```bash
./gradlew test
```

### Native Tests

```bash
./gradlew nativeTest
```

This compiles your tests into a native executable and runs them, ensuring native compatibility.

## Container Images

### Using Cloud Native Buildpacks

Gradle provides built-in support for creating optimized container images:

```bash
# Create a JVM-based container image
./gradlew bootBuildImage

# Create a native container image (takes 4-6 minutes)
./gradlew bootBuildImage -PnativeImage

# Alternative: Use environment variable
BP_NATIVE_IMAGE=true ./gradlew bootBuildImage
```

**Verify the images were created:**
```bash
# List Docker images
docker images | grep management

# You should see: management  0.0.1-SNAPSHOT
```

**Run the container:**
```bash
# Run JVM image
docker run --rm -p 8080:8080 management:0.0.1-SNAPSHOT

# Run native image
docker run --rm -p 8080:8080 management:0.0.1-SNAPSHOT
```

The native image starts in ~0.15 seconds vs ~6 seconds for JVM!

### Multi-Stage Dockerfile

For production, use a multi-stage build for maximum optimization:

```dockerfile
# Stage 1: Build the native image
FROM ghcr.io/graalvm/native-image-community:21 AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew nativeCompile --no-daemon

# Stage 2: Distroless runtime image
FROM gcr.io/distroless/cc-debian12
COPY --from=builder /workspace/build/native/nativeCompile/management /app
EXPOSE 8080
ENTRYPOINT ["/app"]
```

Build and run:
```bash
# Build the image
docker build -t management-native:latest .

# Run with observability
docker run -p 8080:8080 \
  -e SPRING_R2DBC_URL=r2dbc:postgresql://host.docker.internal:26257/management_db \
  -e MANAGEMENT_OTLP_TRACING_ENDPOINT=http://host.docker.internal:4318/v1/traces \
  management-native:latest
```

**Result**: A container image often under **100MB** with sub-second startup!

## Performance Benchmarks

### Startup Time

**JVM:**
```bash
time java -jar build/libs/management-0.0.1-SNAPSHOT.jar
# Expected: Started ManagementApplication in 6.234 seconds
```

**Native:**
```bash
time ./build/native/nativeCompile/management
# Expected: Started ManagementApplication in 0.156 seconds
```

**Result**: ~**40x faster startup**

### Memory Usage

**Check JVM process:**
```bash
ps -ef | grep "java -jar"
ps -o rss= -p <PID>
# Expected: ~550MB
```

**Check Native process:**
```bash
ps -ef | grep "build/native"
ps -o rss= -p <PID>
# Expected: ~140MB
```

**Result**: ~**75% memory reduction**

### Under Load

Generate some load and compare:

```bash
# Install hey (HTTP load generator)
# brew install hey  # macOS
# apt-get install hey  # Ubuntu

# Load test
hey -n 1000 -c 10 http://localhost:8080/actuator/health
```

**Observe**: Both JVM and native versions handle the load well, but native uses significantly less memory.

## Troubleshooting

### Issue: Native compilation fails with OTLP errors

**Solution**: The native compilation might fail if it can't reach the OTLP endpoint. This is expected during build. The application will connect at runtime.

### Issue: Traces not appearing in Jaeger

**Check**:
1. Is Jaeger running? `curl http://localhost:16686`
2. Is the OTLP endpoint configured? Check `application.yml`:
   ```yaml
   management:
     otlp:
       tracing:
         endpoint: http://localhost:4318/v1/traces
   ```
3. Is your application actually sending requests?

### Issue: Native executable crashes on startup

**Common causes**:
- Missing database connection
- Missing runtime hints for reflection

**Debug**: Run with verbose logging:
```bash
SPRING_PROFILES_ACTIVE=debug ./build/native/nativeCompile/management
```

### Issue: "Failed to publish metrics to OTLP receiver"

**This is normal** if the OTLP endpoint isn't running. The application will still work; it just won't export metrics.

**To fix**: Start Jaeger or configure a test profile:
```bash
./gradlew nativeCompile -Pspring.profiles.active=test
```

## Advanced: Runtime Hints

If you encounter issues with third-party libraries not working in native mode, you may need to add runtime hints.

Create a `RuntimeHintsRegistrar`:

```java
package com.apress.crm.management;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public class ManagementRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register reflection hints
        hints.reflection()
            .registerType(TypeReference.of("com.external.Library"));

        // Register resource patterns
        hints.resources()
            .registerPattern("config/*.json");
    }
}
```

Register in `src/main/resources/META-INF/spring/aot.factories`:
```properties
org.springframework.aot.hint.RuntimeHintsRegistrar=\
  com.apress.crm.management.ManagementRuntimeHints
```

## Performance Summary

| Metric | JVM (WebFlux + OTel) | Native | Improvement |
|--------|---------------------|--------|-------------|
| **Startup Time** | ~6.2s | ~0.16s | **39x faster** |
| **Memory (Idle)** | ~550MB | ~140MB | **75% reduction** |
| **Memory (Load)** | ~750MB | ~220MB | **71% reduction** |
| **Container Size** | ~350MB | ~95MB | **73% smaller** |
| **Cold Start** | 6s+ | <0.2s | **Perfect for serverless** |

## Key Takeaways

✅ **Reactive applications work in native mode** - Project Reactor is fully compatible
✅ **OpenTelemetry works seamlessly** - Full distributed tracing with OTLP export
✅ **Custom observability features work** - ObservationHandler, custom metrics, everything
✅ **Testcontainers work** - Full integration testing in native mode
✅ **Production ready** - Spring Boot 4's AOT engine handles complex scenarios

## Learn More

- [Spring Boot Native Documentation](https://docs.spring.io/spring-boot/reference/native-image/)
- [OpenTelemetry in Native Images](https://opentelemetry.io/docs/languages/java/getting-started/)
- [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/)
- Chapter 14: Going Native with Spring Boot and GraalVM
