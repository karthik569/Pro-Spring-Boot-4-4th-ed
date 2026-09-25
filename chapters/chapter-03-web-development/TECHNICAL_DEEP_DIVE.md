# Technical Deep Dive: Chapter 3 - Web Development, Validation & RFC 7807

## 1. Architectural Overview
Chapter 3 focuses on enterprise RESTful API engineering: content negotiation, JSR-380 input validation, and RFC 7807 Problem Details for standardized HTTP error responses.

![Validation and RFC 7807 flow](web-validation-flow.png)

<details>
<summary>Mermaid source (re-renderable)</summary>

```mermaid
%%{init: {'theme':'base','themeVariables':{'primaryColor':'#e0f2fe','primaryBorderColor':'#0284c7','lineColor':'#64748b'},'flowchart':{'htmlLabels':true,'curve':'basis'}}}%%
flowchart TD
    REQ["HTTP Request"] --> MC["Jackson deserializes body"]
    MC --> V{{"Validation on Customer"}}
    V -->|name blank| FAIL["Constraint violations"]
    V -->|email invalid| FAIL
    V -->|phone pattern| FAIL
    V -->|all pass| CTRL["CustomerController"]
    CTRL --> REPO[("CustomerRepository")]
    REPO --> RESP["201 Created + Location"]
    FAIL --> EX["MethodArgumentNotValidException"]
    EX --> ADV["GlobalExceptionHandler"]
    ADV --> PD["ProblemDetail RFC 7807<br/>title / status / detail / instance / errors"]
    PD --> ERR["400 application/problem+json"]
    classDef ok fill:#dcfce7,stroke:#15803d,color:#14532d;
    classDef err fill:#fee2e2,stroke:#b91c1c,color:#7f1d1d;
    classDef store fill:#ede9fe,stroke:#7c3aed,color:#4c1d95;
    class RESP,MC,CTRL ok;
    class FAIL,EX,ADV,PD,ERR err;
    class REPO store;
```

</details>

## 2. Implementation Mechanics
- **Model Validation**:
  ```java
  public record Customer(
      UUID id,
      @NotBlank(message = "First name is mandatory") String firstName,
      @NotBlank(message = "Last name is mandatory") String lastName,
      @Email(message = "Invalid email format") String email
  ) {}
  ```
- **RFC 7807 ProblemDetail Integration**:
  - Implements `@ExceptionHandler(MethodArgumentNotValidException.class)`.
  - Constructs `ProblemDetail` with URI type definitions and field-level validation violation summaries.

## 3. Build & Test
```bash
cd chapters/chapter-03-web-development/customer
mvn clean test
```

## 4. Key Takeaways
- Standardization of error contracts across microservices eliminates custom error schema drift.
- Never return raw stack traces or internal exception class names in production responses.
