# Technical Deep Dive: Chapter 14 - GraalVM Native Image, AOT & CRaC

## 1. Architectural Overview
Chapter 14 covers extreme performance optimizations: compiling Spring Boot applications into standalone **GraalVM Native Executables** and utilizing **CRaC (Coordinated Restore at Checkpoint)** for near-instant cold starts.

```mermaid
graph LR
    SRC[Java Source Code] --> AOT_PROC[Spring AOT Processing<br/>Bytecode Generation]
    AOT_PROC --> GRAALVM[GraalVM native-image compiler]
    GRAALVM --> BIN[Standalone Native Binary<br/>Instant Startup & Low RAM]
```

## 2. Core Technical Mechanics
### Ahead-of-Time (AOT) Optimization
Spring AOT inspects bean definitions at build time, replacing runtime reflection with explicit factory declarations.

### Explicit Runtime Hints (`RuntimeHintsRegistrar`)
When classes are accessed reflectively by third-party serializers or dynamic agents, explicit hints prevent native image stripping:
```java
public class ManagementRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection().registerType(CustomerDetailsDTO.class, 
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, 
            MemberCategory.DECLARED_FIELDS);
    }
}
```

### CRaC Lifecycle Coordination
Implements `org.crac.Resource` to gracefully close sockets before checkpointing and reconnect after snapshot restoration.

## 3. Build & Verification
```bash
cd chapters/chapter-14-native-aot/customer && mvn -Pnative native:compile
cd ../management && bash ./gradlew nativeCompile
```
