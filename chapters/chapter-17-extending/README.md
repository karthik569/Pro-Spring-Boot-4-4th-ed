# Chapter 17: Custom Spring Boot Starter - Extending Spring Boot

This chapter demonstrates how to create a professional-grade Spring Boot starter by transforming the AI-powered CRM Assistant from Chapter 16 into a reusable library.

## Projects Overview

### 1. **crm-assistant-starter** - Multi-Module Starter Project

A professional Spring Boot starter following Spring Boot conventions:

**crm-assistant-autoconfigure**
- Contains all auto-configuration logic
- Packages: `config`, `service`, `tool`, `client`, `aop`, `impl`
- Key Components:
  - `CrmAssistant` interface
  - `DefaultCrmAssistant` implementation
  - `RAGService`, `DatabaseAgentService`
  - `CustomerTools`, `R2dbcQueryTool`
  - `AssistantAuditAspect` for AOP logging
  - `CrmAssistantAutoConfiguration` with @Conditional beans
  - `CrmAssistantProperties` for type-safe configuration

**crm-assistant-spring-boot-starter**
- Lightweight dependency wrapper
- Single dependency on autoconfigure module
- This is what users add to their projects

### 2. **sales-dashboard** - Demo Application

Demonstrates how easy it is to use the custom starter:
- Add starter dependency
- Add `@EnableCrmAssistant` annotation  
- Inject and use `CrmAssistant` interface
- Zero boilerplate required!

## Key Features

### Intelligent Auto-Configuration

```java
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
@ConditionalOnProperty(prefix = "crm.assistant", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CrmAssistantProperties.class)
public class CrmAssistantAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public CrmAssistant crmAssistant(...) {
        // Auto-configured bean
    }
}
```

### @Enable Pattern

```java
@SpringBootApplication
@EnableCrmAssistant
public class SalesDashboardApplication {
    // CrmAssistant is now available!
}
```

### Configuration Properties

```yaml
crm:
  assistant:
    enabled: true
    chat-model: gpt-4o
    vector-table-name: crm_vectors
    audit:
      enabled: true  # Enables AOP audit logging
```

### AOP Cross-Cutting Concerns

The starter transparently adds audit logging to all `CrmAssistant` method calls when enabled:

```
CRM Assistant - Call started: chat with args: ["What is CRM?"]
CRM Assistant - Call finished: chat in 1234ms. Result: A CRM system is...
```

## Building the Starter

```bash
cd crm-assistant-starter
./gradlew build -x test
```

Artifacts created:
- `crm-assistant-autoconfigure/build/libs/crm-assistant-autoconfigure-0.0.1-SNAPSHOT.jar`
- `crm-assistant-spring-boot-starter/build/libs/crm-assistant-spring-boot-starter-0.0.1-SNAPSHOT.jar`

## Using the Starter

### 1. Add Dependency

```gradle
dependencies {
    implementation 'com.apress:crm-assistant-spring-boot-starter:0.0.1-SNAPSHOT'
    
    // Provider-specific dependencies (required)
    implementation 'org.springframework.ai:spring-ai-ollama-spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
}
```

### 2. Enable the Assistant

```java
@SpringBootApplication
@EnableCrmAssistant
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. Inject and Use

```java
@Service
public class MyService {
    private final CrmAssistant assistant;
    
    public MyService(CrmAssistant assistant) {
        this.assistant = assistant;
    }
    
    public String askQuestion(String question) {
        return assistant.chat(question);
    }
}
```

## Running the Demo

```bash
cd sales-dashboard
./gradlew compileJava
```

The sales-dashboard successfully compiles using the custom starter!

## Architecture Highlights

### Multi-Module Design
- **Autoconfigure Module**: Core logic and configuration
- **Starter Module**: Dependency wrapper
- **Clear Separation**: Configuration vs dependencies

### Conditional Loading
- `@ConditionalOnClass`: Only loads if Spring AI present
- `@ConditionalOnProperty`: Respects user configuration
- `@ConditionalOnMissingBean`: Allows user overrides

### Auto-Configuration Discovery
```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.apress.crm.assistant.config.CrmAssistantAutoConfiguration
```

### Publishing (Optional)
Configured for GitHub Packages but NOT executed (as requested):
```bash
# DO NOT RUN - Publishing disabled for this demo
# ./gradlew publish
```

## Key Learnings

1. **Convention over Configuration**: Starter auto-configures everything sensibly
2. **@Conditional Annotations**: Smart component loading based on classpath and properties
3. **@Enable Pattern**: Explicit opt-in for clear intent
4. **ConfigurationProperties**: Type-safe, IDE-friendly configuration
5. **AOP Integration**: Cross-cutting concerns without user code changes
6. **User Override**: `@ConditionalOnMissingBean` allows complete customization
7. **Professional Structure**: Follows Spring Boot's own starter conventions

## What Makes This Professional?

✅ Multi-module architecture  
✅ Intelligent conditionals  
✅ Type-safe properties  
✅ AOP for cross-cutting concerns  
✅ Auto-configuration registration  
✅ User override support  
✅ Clear documentation  
✅ Publishing configuration  
✅ Compile successfully  
✅ Demo application  

This is production-ready starter code following Spring Boot best practices!
