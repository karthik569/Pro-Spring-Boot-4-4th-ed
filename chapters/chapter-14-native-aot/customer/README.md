# Customer CRM - Native Image with GraalVM

This project demonstrates how to compile a Spring Boot application into a native executable using GraalVM Native Image. The native executable offers near-instantaneous startup times and drastically reduced memory consumption.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Building the Application](#building-the-application)
- [Running the Application](#running-the-application)
- [Benchmarking: JVM vs Native](#benchmarking-jvm-vs-native)
- [Testing](#testing)
- [Container Images](#container-images)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### 1. Install GraalVM

The easiest way to install GraalVM is via SDKMAN!:

```bash
# Install SDKMAN if you haven't already
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Liberica NIK (Native Image Kit) for Java 21
sdk install java 23.1.2.r21-nik

# Set it as the default for your current shell
sdk use java 23.1.2.r21-nik

# Verify installation
java -version
native-image --version
```

### 2. System Requirements

- **Memory**: At least 8GB RAM (16GB recommended for native compilation)
- **Disk Space**: ~2GB free space for build artifacts
- **Time**: Native compilation takes 2-5 minutes depending on your system

## Building the Application

### Standard JVM Build

```bash
# Clean and build the JAR
./mvnw clean package

# The JAR will be in target/customer-0.0.1-SNAPSHOT.jar
```

### Native Image Build

```bash
# Build the native executable (this will take several minutes)
./mvnw clean package -Pnative

# The native executable will be in target/customer
```

**Build Options:**

```bash
# Build with verbose output
./mvnw clean package -Pnative -Dverbose=true

# Skip tests during build
./mvnw clean package -Pnative -DskipTests
```

## AOT Features Demonstrated

This project showcases Spring Boot 4's AOT (Ahead-of-Time) processing features:

### 1. Runtime Hints (`CustomerRuntimeHints`)

Location: `customer/src/main/java/com/apress/crm/customer/aot/CustomerRuntimeHints.java`

Provides hints to GraalVM for:
- **Reflection**: Registers `Customer` class for reflection (serialization, constructors, methods)
- **Resources**: Includes configuration files and database migrations in native image
- **Serialization**: Enables Customer object serialization in native mode

```java
// Spring Boot 4 compatible - uses INVOKE_* member categories
hints.reflection().registerType(
    TypeReference.of(Customer.class),
    hint -> hint.withMembers(
        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
        MemberCategory.INVOKE_DECLARED_METHODS,
        MemberCategory.INVOKE_PUBLIC_METHODS
    )
);

hints.resources()
    .registerPattern("customer-*.properties")
    .registerPattern("db/migration/*.sql");
```

### 2. Programmatic Bean Registration (`CustomerBeanRegistrar`)

Location: `customer/src/main/java/com/apress/crm/customer/aot/CustomerBeanRegistrar.java`

Demonstrates AOT-friendly conditional bean registration:
- Registers custom executor using Virtual Threads (Java 21)
- Conditional beans based on environment (K8s vs local)
- Works seamlessly in native mode

```java
@Import(CustomerBeanRegistrar.class)  // Enabled in CustomerConfiguration
```

### 3. AOT Factories Registration

Location: `customer/src/main/resources/META-INF/spring/aot.factories`

Registers the runtime hints with Spring's AOT engine:
```properties
org.springframework.aot.hint.RuntimeHintsRegistrar=\
  com.apress.crm.customer.aot.CustomerRuntimeHints
```

These AOT features ensure the application works correctly when compiled to native.

## Running the Application

### Start CockroachDB

The application requires a database. Use the provided `docker-compose.yml`:

```bash
# Start CockroachDB
docker-compose up -d

# Check if it's running
docker-compose ps
```

### Run the JVM Version

```bash
java -jar target/customer-0.0.1-SNAPSHOT.jar
```

**Expected output:**
```
Started CustomerApplication in 4.875 seconds (process running for 5.234)
```

### Run the Native Executable

```bash
./target/customer
```

**Expected output:**
```
Started CustomerApplication in 0.098 seconds (process running for 0.112)
```

**Notice the difference**: The native executable starts **~50x faster**!

## Benchmarking: JVM vs Native

### 1. Startup Time Comparison

**JVM Version:**
```bash
time java -jar target/customer-0.0.1-SNAPSHOT.jar
# Observe: Started CustomerApplication in 4.875 seconds
```

**Native Version:**
```bash
time ./target/customer
# Observe: Started CustomerApplication in 0.098 seconds
```

### 2. Memory Consumption Comparison

While both applications are running, open a separate terminal:

**For JVM process:**
```bash
# Find the process ID
ps -ef | grep "java -jar" | grep "customer"

# Check memory usage (RSS - Resident Set Size)
ps -o rss= -p <JVM_PID>
# Expected: ~450000 (which is ~450MB)
```

**For Native process:**
```bash
# Find the process ID
ps -ef | grep "target/customer"

# Check memory usage
ps -o rss= -p <NATIVE_PID>
# Expected: ~125000 (which is ~125MB)
```

**Result**: The native executable uses **~70% less memory**!

### 3. File Size Comparison

```bash
ls -lh target/customer-0.0.1-SNAPSHOT.jar
# Expected: ~50-60MB (JAR + dependencies)

ls -lh target/customer
# Expected: ~80-100MB (standalone executable, no JVM needed)
```

## Testing

### Standard Tests

```bash
# Run tests normally
./mvnw test
```

### Native Tests

Run your test suite within a native image to catch compatibility issues:

```bash
# This compiles tests into a native executable and runs them
./mvnw test -Pnative
```

**Note**: Native tests take longer to run but ensure your application works correctly when compiled natively.

## Container Images

### Using Cloud Native Buildpacks

Spring Boot includes built-in support for creating optimized container images:

```bash
# Create a JVM-based container image
./mvnw spring-boot:build-image

# Create a native container image (takes 3-5 minutes)
./mvnw spring-boot:build-image -Pnative
```

**Verify the images were created:**
```bash
# List Docker images
docker images | grep customer

# You should see: customer  0.0.1-SNAPSHOT
```

**Run the container:**
```bash
# Run JVM image
docker run --rm -p 8080:8080 customer:0.0.1-SNAPSHOT

# Run native image
docker run --rm -p 8080:8080 customer:0.0.1-SNAPSHOT
```

The native image will be significantly smaller and start faster in containerized environments.

### Custom Dockerfile

For production deployments, you can create a multi-stage Dockerfile:

```dockerfile
# Stage 1: Build the native image
FROM ghcr.io/graalvm/native-image-community:21 AS builder
WORKDIR /workspace
COPY . .
RUN ./mvnw clean package -Pnative -DskipTests

# Stage 2: Create minimal runtime image
FROM ubuntu:22.04
RUN apt-get update && apt-get install -y libz-dev && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/target/customer /app
EXPOSE 8080
ENTRYPOINT ["/app"]
```

Build and run:
```bash
docker build -t customer-native .
docker run -p 8080:8080 customer-native
```

## Troubleshooting

### Issue: "native-image: command not found"

**Solution**: Make sure you're using a GraalVM distribution with Native Image:
```bash
sdk use java 23.1.2.r21-nik
native-image --version
```

### Issue: Build fails with "Out of Memory"

**Solution**: Increase heap size for the native-image build:
```bash
export MAVEN_OPTS="-Xmx8g"
./mvnw clean package -Pnative
```

### Issue: Runtime errors about missing classes or reflection

**Solution**: You may need to add runtime hints. See Chapter 14 for details on `RuntimeHintsRegistrar`.

### Issue: Slow native compilation

**Solution**:
- Use AOT caching (add `<cacheAot>true</cacheAot>` to plugin configuration)
- Build on a machine with more CPU cores and RAM
- Use `-DskipTests` to skip test compilation

## API Endpoints

Once running, you can test the application:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Get all customers
curl http://localhost:8080/api/customers

# Create a customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","phone":"555-1234"}'
```

## Performance Summary

| Metric | JVM | Native | Improvement |
|--------|-----|--------|-------------|
| **Startup Time** | ~4.9s | ~0.1s | **49x faster** |
| **Memory Usage** | ~450MB | ~125MB | **72% reduction** |
| **Build Time** | ~10s | ~3min | Slower (trade-off) |
| **Container Size** | ~300MB | ~100MB | **67% smaller** |

## Learn More

- [Spring Boot Native Documentation](https://docs.spring.io/spring-boot/reference/native-image/introducing-graalvm-native-images.html)
- [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/)
- Chapter 14: Going Native with Spring Boot and GraalVM
