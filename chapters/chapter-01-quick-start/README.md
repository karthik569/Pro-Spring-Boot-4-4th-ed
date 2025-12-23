# Chapter 1: Spring Boot - Quick Start

This directory contains the source code for Chapter 1 of the book "Pro Spring Boot 4".

## Learning Objectives
- Understand Spring Boot fundamentals.
- Create your first Spring Boot application using Spring Initializr.
- Explore the basic project structure and key configuration files.
- Define data models using modern Java records.
- Implement data access logic with a repository pattern.
- Build RESTful APIs using Spring MVC.
- Populate initial data for application testing.
- Run and manually test Spring Boot applications.
- Write automated integration tests for REST endpoints using `@SpringBootTest` and `RestTestClient`.
- Understand the core principles and benefits of Spring Boot.

## Key Topics
- Spring Initializr for project bootstrapping.
- Maven `pom.xml` structure and essential dependencies (`spring-boot-starter-webmvc`, `spring-boot-starter-webmvc-test`).
- Java Records for immutable data models.
- Repository pattern for data access (in-memory `ConcurrentHashMap` implementation).
- REST Controller development with `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@DeleteMapping`, `@RequestBody`, and `@PathVariable`.
- Dependency Injection via constructor injection.
- Application lifecycle events (`ApplicationReadyEvent`) for data initialization.
- Running Spring Boot applications.
- Automated testing with `@SpringBootTest`, `RestTestClient`, and `@AutoConfigureRestTestClient`.
- Introduction to Reactive Web Applications with Spring WebFlux (Management CRM example).
- Spring Framework principles and Spring Boot's opinionated approach.
