# Chapter 14: Going Native with Spring Boot and GraalVM

This chapter demonstrates how to compile Spring Boot applications into **native executables** using **GraalVM Native Image**. Native executables offer dramatic improvements in startup time and memory consumption, making them ideal for cloud-native deployments, microservices, and serverless functions.

## Projects Overview

This chapter contains two case studies with **complete AOT implementations**:

### 1. **Customer CRM** (Maven)
A Spring Boot application demonstrating:
- Native compilation with Maven
- Basic Spring Boot features (Web MVC, JPA, Security, Actuator)
- **RuntimeHintsRegistrar** for reflection and resource hints
- **ImportBeanDefinitionRegistrar** for programmatic bean registration
- Performance benchmarking (JVM vs Native)
- Container image optimization

**Location**: `customer/`
**Build Tool**: Maven
**Key Features**: Web MVC, JPA, Actuator, Prometheus Metrics
**AOT Examples**: ✅ RuntimeHints, ✅ Bean Registration, ✅ aot.factories

👉 [Customer Project README](./customer/README.md)

### 2. **Management CRM** (Gradle)
A more complex reactive application demonstrating:
- Native compilation with Gradle
- Reactive programming (WebFlux, R2DBC)
- **OpenTelemetry distributed tracing in native mode**
- **Advanced RuntimeHintsRegistrar** for reactive entities
- **Cloud-aware bean registration** (K8s, Cloud Foundry)
- Custom Observability features
- Proof that complex Spring Boot 4 features work natively

**Location**: `management/`
**Build Tool**: Gradle
**Key Features**: WebFlux, R2DBC, OpenTelemetry, Custom Actuator Endpoints
**AOT Examples**: ✅ Advanced RuntimeHints, ✅ Cloud-aware Bean Registration, ✅ aot.factories

👉 [Management Project README](./management/README.md)

## Quick Start

### Prerequisites

Both projects require GraalVM with Native Image support:

```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install GraalVM (Liberica NIK)
sdk install java 23.1.2.r21-nik
sdk use java 23.1.2.r21-nik

# Verify
java -version
native-image --version
```

### Build Both Projects

**Customer (Maven):**
```bash
cd customer
./mvnw clean package -Pnative
./target/customer
```

**Management (Gradle):**
```bash
cd management
./gradlew nativeCompile
./build/native/nativeCompile/management
```

## Key Concepts Demonstrated

### 1. **AOT vs JIT Compilation**

| Feature | JVM (JIT) | GraalVM Native (AOT) |
|---------|-----------|---------------------|
| Startup Time | Slow (seconds) | Fast (milliseconds) |
| Memory Usage | High | Low |
| Build Time | Fast | Slow (minutes) |
| Peak Performance | Excellent | Good |
| Flexibility | High (dynamic) | Limited (static) |

### 2. **Spring AOT Engine**

Spring Boot 4 includes an **AOT-First** design that:
- Analyzes your application at build time
- Generates source code for dynamic behavior
- Provides hints to GraalVM for reflection, resources, etc.
- Makes native compilation seamless

### 3. **Performance Improvements**

Expected improvements when running native:

| Metric | Customer | Management |
|--------|----------|------------|
| **Startup** | ~50x faster | ~40x faster |
| **Memory** | ~70% reduction | ~75% reduction |
| **Container** | ~65% smaller | ~70% smaller |

### 4. **Native Compatibility**

Both projects prove that complex Spring features work in native mode:
- ✅ Spring MVC / WebFlux
- ✅ JPA / R2DBC
- ✅ Security
- ✅ Actuator
- ✅ Metrics (Micrometer + Prometheus)
- ✅ **OpenTelemetry Distributed Tracing**
- ✅ Custom Observation Handlers
- ✅ Testcontainers

## Advanced Topics Covered

Both projects include working implementations of all AOT features!

### Runtime Hints (Implemented in Both Projects)

**Customer Project** (`CustomerRuntimeHints`):
- Basic runtime hints for reflection, resources, and serialization
- Example: Customer class registration for JSON serialization

**Management Project** (`ManagementRuntimeHints`):
- Advanced hints for reactive applications
- Registers all R2DBC entities, DTOs, and resources
- Demonstrates comprehensive reflection hints

When third-party libraries use reflection or load resources dynamically, you may need to provide hints:

```java
public class CrmRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Reflection hints (Spring Boot 4 style - use INVOKE_* categories)
        hints.reflection().registerType(
            TypeReference.of("com.external.Library"),
            hint -> hint.withMembers(
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS
            )
        );

        // Resource hints
        hints.resources().registerPattern("config/*.json");
    }
}
```

Register in `src/main/resources/META-INF/spring/aot.factories`:
```properties
org.springframework.aot.hint.RuntimeHintsRegistrar=\
  com.apress.crm.CrmRuntimeHints
```

### Programmatic Bean Registration (Implemented in Both Projects)

**Customer Project** (`CustomerBeanRegistrar`):
- Registers custom executor using Java 21 Virtual Threads
- Conditional beans based on environment (K8s vs local)
- Simple example of ImportBeanDefinitionRegistrar

**Management Project** (`ManagementBeanRegistrar`):
- Cloud platform detection (Kubernetes, Cloud Foundry)
- Reactive-specific bean registration (WebClient)
- Advanced conditional logic for different deployment environments

For AOT-friendly conditional bean registration:

```java
public class CrmBeanRegistrar implements AotBeanRegistrar {
    @Override
    public void register(BeanDefinitionRegistry registry, Environment env) {
        if (CloudPlatform.isKubernetes(env)) {
            BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.rootBeanDefinition(K8sProbe.class);
            registry.registerBeanDefinition("k8sProbe",
                builder.getBeanDefinition());
        }
    }
}
```

### Container Optimization

Both projects support building optimized container images:

**Cloud Native Buildpacks (easiest):**
```bash
# Maven - JVM image
cd customer
./mvnw spring-boot:build-image

# Maven - Native image (3-5 minutes)
./mvnw spring-boot:build-image -Pnative

# Gradle - JVM image
cd management
./gradlew bootBuildImage

# Gradle - Native image (4-6 minutes)
./gradlew bootBuildImage -PnativeImage
```

**Verify images were created:**
```bash
docker images | grep -E "customer|management"
```

**Multi-stage Dockerfile (smallest):**
```dockerfile
FROM ghcr.io/graalvm/native-image-community:21 AS builder
WORKDIR /workspace
COPY . .
RUN ./mvnw package -Pnative

FROM gcr.io/distroless/cc-debian12
COPY --from=builder /workspace/target/customer /app
ENTRYPOINT ["/app"]
```

Result: Container images often **under 100MB** with **sub-second startup**!

## Testing Native Applications

Both projects support native testing:

**Maven:**
```bash
./mvnw test -Pnative
```

**Gradle:**
```bash
./gradlew nativeTest
```

This compiles your tests into a native executable and runs them, catching native compatibility issues early.

## When to Use Native Images

**Good fit:**
- ☁️ Cloud-native microservices
- ⚡ Serverless functions (AWS Lambda, etc.)
- 🚀 Applications with frequent cold starts
- 💰 Cost-sensitive deployments (memory = money)
- 🐳 Containerized applications
- 📦 CLI tools

**May not be ideal:**
- Long-running applications (JIT optimizations matter more)
- Applications using heavy reflection/dynamic features
- Development environments (slow build times)

## Troubleshooting

### Common Issues

1. **"native-image: command not found"**
   - Solution: Use GraalVM distribution with Native Image

2. **Out of Memory during build**
   - Solution: `export MAVEN_OPTS="-Xmx8g"`

3. **Missing classes at runtime**
   - Solution: Add runtime hints for reflection

4. **Slow build times**
   - Solution: Enable AOT caching, use faster hardware

See individual project READMEs for detailed troubleshooting.

## Performance Benchmarking

Each project README includes detailed benchmarking instructions:

1. **Startup Time**: Compare JVM vs Native startup
2. **Memory Usage**: Measure RSS of both processes
3. **Container Size**: Compare image sizes
4. **Load Testing**: Verify performance under load

## Learning Path

1. **Start with Customer**: Simpler project, easier to understand
2. **Move to Management**: Complex features, proves native maturity
3. **Experiment**: Try adding features, see if they work natively
4. **Optimize**: Use hints, programmatic registration as needed
5. **Deploy**: Build container images, deploy to cloud

## Additional Resources

- [Spring Boot Native Documentation](https://docs.spring.io/spring-boot/reference/native-image/)
- [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/)
- [Cloud Native Buildpacks](https://buildpacks.io/)
- Book: Pro Spring Boot 4, Chapter 14

## Summary

This chapter demonstrates that:
- ✅ Spring Boot 4 has **first-class native support**
- ✅ Complex features like **OpenTelemetry work natively**
- ✅ Native executables offer **dramatic performance improvements**
- ✅ Build tools (Maven & Gradle) make it **easy to enable**
- ✅ Spring's AOT engine handles most **dynamic behavior automatically**

Native compilation is a powerful deployment option that can significantly reduce costs and improve user experience in cloud-native environments.

---

**Next Steps**: Dive into the individual project READMEs for hands-on instructions! 🚀
