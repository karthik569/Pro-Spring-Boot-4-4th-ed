# AOT Features Quick Reference Guide

This guide shows where to find all AOT (Ahead-of-Time) compilation examples in Chapter 14.

## Customer Project (Maven) - Basic AOT Features

### 📁 File Structure
```
customer/
├── src/main/java/com/apress/crm/customer/
│   ├── aot/
│   │   ├── CustomerRuntimeHints.java       ⭐ Runtime hints
│   │   └── CustomerBeanRegistrar.java      ⭐ Bean registration
│   └── CustomerConfiguration.java          (imports bean registrar)
└── src/main/resources/
    └── META-INF/spring/
        └── aot.factories                    ⭐ AOT registration
```

### 🎯 What Each File Does

**CustomerRuntimeHints.java**
- Purpose: Provides hints to GraalVM for reflection, resources, serialization
- Key Patterns:
  ```java
  hints.reflection().registerType(...)     // Reflection hints
  hints.resources().registerPattern(...)   // Resource patterns
  hints.serialization().registerType(...)  // Serialization hints
  ```

**CustomerBeanRegistrar.java**
- Purpose: Programmatic bean registration for AOT
- Key Patterns:
  ```java
  BeanDefinitionBuilder.rootBeanDefinition(...)
  registry.registerBeanDefinition(...)
  System.getenv("KUBERNETES_SERVICE_HOST")  // Environment detection
  ```

**aot.factories**
- Purpose: Register hints with Spring's AOT engine
- Content:
  ```properties
  org.springframework.aot.hint.RuntimeHintsRegistrar=\
    com.apress.crm.customer.aot.CustomerRuntimeHints
  ```

## Management Project (Gradle) - Advanced AOT Features

### 📁 File Structure
```
management/
├── src/main/java/com/apress/crm/management/
│   ├── aot/
│   │   ├── ManagementRuntimeHints.java     ⭐ Advanced runtime hints
│   │   └── ManagementBeanRegistrar.java    ⭐ Cloud-aware registration
│   ├── crac/
│   │   ├── CracManagedConnection.java      🔄 CRaC Resource implementation
│   │   └── NetworkConnection.java          (helper class)
│   └── configuration/
│       └── ManagementConfiguration.java    (imports bean registrar)
└── src/main/resources/
    └── META-INF/spring/
        └── aot.factories                    ⭐ AOT registration
```

### 🎯 What Each File Does

**ManagementRuntimeHints.java**
- Purpose: Comprehensive hints for reactive R2DBC applications
- Key Patterns:
  ```java
  // Register multiple domain models
  Class<?>[] domainClasses = {Customer.class, Address.class, ...};
  
  // Register with specific member categories
  hints.reflection().registerType(
      TypeReference.of(clazz),
      hint -> hint.withMembers(
          MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
          MemberCategory.INVOKE_PUBLIC_METHODS
      )
  );
  ```

**ManagementBeanRegistrar.java**
- Purpose: Advanced conditional bean registration based on cloud platform
- Key Patterns:
  ```java
  // Kubernetes detection
  System.getenv("KUBERNETES_SERVICE_HOST") != null
  
  // Cloud Foundry detection
  System.getenv("VCAP_APPLICATION") != null
  
  // Reactive WebClient registration
  BeanDefinitionBuilder.genericBeanDefinition(WebClient.Builder.class)
  ```

**aot.factories**
- Purpose: Register hints for reactive application
- Content:
  ```properties
  org.springframework.aot.hint.RuntimeHintsRegistrar=\
    com.apress.crm.management.aot.ManagementRuntimeHints
  ```

### 🔄 CRaC Support (Management Project Only)

**Location**: `management/src/main/java/com/apress/crm/management/crac/`

The Management project includes a demonstration of **CRaC (Coordinated Restore at Checkpoint)**, which allows taking snapshots of running JVM applications and restoring them with near-instant startup times.

**Key Files:**
- `CracManagedConnection.java` - Implements `org.crac.Resource` interface
- `NetworkConnection.java` - Helper class simulating a managed connection

**CracManagedConnection.java**
- Purpose: Demonstrates resource lifecycle management during checkpoint/restore
- Key Patterns:
  ```java
  @Component
  @ConditionalOnProperty(name = "management.crac.enabled", havingValue = "true")
  public class CracManagedConnection implements Resource {

      public CracManagedConnection() {
          // Register with global CRaC context
          Core.getGlobalContext().register(this);
      }

      @Override
      public void beforeCheckpoint(Context<? extends Resource> context) {
          // Close all I/O resources before checkpoint
          closeConnection();
      }

      @Override
      public void afterRestore(Context<? extends Resource> context) {
          // Reopen resources after restore
          openConnection();
      }
  }
  ```

**Why CRaC?**
- Combines benefits of JVM (JIT optimizations) with native-like startup times
- Preserves warmed-up JVM state including JIT-compiled code
- Ideal for applications that need both fast startup AND peak performance

**When to Use:**
- Serverless platforms where you want JIT performance
- Applications with long warm-up periods
- Scenarios where you can't use GraalVM Native Image
- When you need the full JVM feature set

**Comparison Table:**

| Feature | JVM | CRaC | Native (GraalVM) |
|---------|-----|------|------------------|
| Startup | ~6s | ~0.1s (restore) | ~0.2s |
| Memory | ~550MB | ~450MB | ~140MB |
| JIT Perf | ✅ Yes | ✅ Preserved | ❌ AOT only |
| Build Time | Fast | Fast | Slow (5+ min) |
| Portability | Any JDK | CRaC JDK required | Native binary |

**How to Use CRaC:**

1. Enable in `application.properties`:
   ```properties
   management.crac.enabled=true
   ```

2. Build and run:
   ```bash
   ./gradlew build
   java -XX:CRaCCheckpointTo=./checkpoint -jar build/libs/management-0.0.1-SNAPSHOT.jar
   ```

3. In another terminal, trigger checkpoint:
   ```bash
   jcmd $(pgrep -f management) JDK.checkpoint
   ```

4. Restore from checkpoint:
   ```bash
   java -XX:CRaCRestoreFrom=./checkpoint
   ```

**Note**: CRaC is **disabled by default** since it requires a CRaC-enabled JDK. The `org.crac:crac` library provides no-op stubs when running on a standard JDK, so the code compiles normally without CRaC support.

## Build Commands

### Customer (Maven)
```bash
cd customer

# Standard build
./mvnw clean package

# Native build (includes AOT processing)
./mvnw clean package -Pnative

# Native container image
./mvnw spring-boot:build-image -Pnative
```

### Management (Gradle)
```bash
cd management

# Standard build
./gradlew clean build

# Native build (includes AOT processing)
./gradlew nativeCompile

# Native container image (JVM)
./gradlew bootBuildImage

# Native container image (Native)
./gradlew bootBuildImage -PnativeImage
```

## How AOT Processing Works

### 1️⃣ **Build Time** (AOT Phase)
```
Source Code + aot.factories
         ↓
Spring AOT Engine reads RuntimeHintsRegistrar
         ↓
Generates AOT-optimized code
         ↓
GraalVM Native Image receives hints
         ↓
Native Executable
```

### 2️⃣ **What Happens During AOT**

Customer Project:
1. Spring reads `customer/src/main/resources/META-INF/spring/aot.factories`
2. Instantiates `CustomerRuntimeHints`
3. Calls `registerHints()` method
4. Generates reflection metadata for Customer class
5. Includes resource patterns in native image
6. Processes `CustomerBeanRegistrar` for bean definitions

Management Project:
1. Spring reads `management/src/main/resources/META-INF/spring/aot.factories`
2. Instantiates `ManagementRuntimeHints`
3. Registers all R2DBC entities for reflection
4. Includes reactive resources in native image
5. Processes `ManagementBeanRegistrar` for cloud-aware beans

## Common Patterns Reference

### Reflection Hints
```java
hints.reflection().registerType(
    TypeReference.of(MyClass.class),
    hint -> hint.withMembers(
        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
        MemberCategory.INVOKE_PUBLIC_METHODS
    )
);
```

### Resource Hints
```java
hints.resources()
    .registerPattern("config/*.properties")
    .registerPattern("db/migration/*.sql");
```

### Serialization Hints
```java
hints.serialization()
    .registerType(TypeReference.of(MyClass.class));
```

### Bean Registration
```java
BeanDefinitionBuilder builder = 
    BeanDefinitionBuilder.rootBeanDefinition(MyBean.class);
builder.addConstructorArgValue("someValue");
registry.registerBeanDefinition("myBean", builder.getBeanDefinition());
```

## Testing AOT Features

### Verify Hints Are Applied
```bash
# Build with verbose output
./mvnw package -Pnative -Dverbose=true

# Check for reflection metadata
grep -r "CustomerRuntimeHints" target/spring-aot/

# Verify native image includes resources
jar tf target/customer | grep "customer-"
```

### Debug Native Compilation
```bash
# Maven - see what GraalVM processes
./mvnw package -Pnative -X

# Gradle - verbose native compilation
./gradlew nativeCompile --info
```

## Troubleshooting

### Issue: RuntimeHints not being applied
**Solution**: Check that:
1. `aot.factories` file is in `src/main/resources/META-INF/spring/`
2. Package name in aot.factories matches your RuntimeHintsRegistrar class
3. File encoding is UTF-8

### Issue: Beans not registered
**Solution**: Ensure:
1. `@Import(YourBeanRegistrar.class)` is on a `@Configuration` class
2. Bean names don't conflict with existing beans
3. BeanDefinitionBuilder uses correct factory methods

### Issue: Missing reflection at runtime
**Solution**: Add explicit hints:
```java
hints.reflection().registerType(
    TypeReference.of(MissingClass.class),
    hint -> hint.withMembers(MemberCategory.values())
);
```

## Next Steps

1. **Explore Customer Project** (`customer/aot/`) - Simple examples
2. **Study Management Project** (`management/aot/`) - Advanced patterns
3. **Build Native Images** - Test with `./mvnw package -Pnative`
4. **Compare Startup Times** - JVM vs Native
5. **Experiment** - Add your own RuntimeHints

---

📚 **Related Documentation:**
- Customer Project: [customer/README.md](./customer/README.md#aot-features-demonstrated)
- Management Project: [management/README.md](./management/README.md#advanced-aot-features)
- Chapter Overview: [README.md](./README.md)
