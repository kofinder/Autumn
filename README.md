# Autumn Framework

Autumn is a lightweight Java dependency injection (DI) and application framework inspired by Spring Boot. It provides a component-based architecture with automatic bean management, lifecycle hooks, and a simple package-scanning mechanism.

Purpose

    * The goal of Autumn is to provide a Spring Boot-like experience for small or educational projects without requiring the full Spring ecosystem. It enables developers to:

    * Define beans using annotations (@Component, @Service, @Repository, @Controller).

    * Automatically wire dependencies via @Autowired (constructor or field injection).

    * Support lifecycle hooks with @PostConstruct.

    * Register custom BeanProcessors to intercept beans for logging, proxies, or additional processing.

    * Bootstrap applications with a single @AutumnApplication annotation.


Usage Summary

    Annotate your main class with @AutumnApplication.

    Annotate your beans with @Component, @Service, @Repository, or @Bean.

    Inject dependencies using @Autowired.

    Use @PostConstruct for initialization logic.

    Use AutumnApplicationRunner.run(Application.class) to bootstrap.

    Optionally add BeanProcessors for custom behavior.



    ┌─────────────────────────────┐
    │         HTTP Request        │
    └─────────────┬──────────────┘
                │
                ▼
    ┌─────────────────────────────┐
    │        Dispatcher           │
    │    (Front Controller)       │
    └─────────────┬──────────────┘
                │
    ┌─────────────┴──────────────┐
    │       Filter Chain         │
    │ (Chain of Responsibility)  │
    └─────────────┬──────────────┘
                │
                ▼
    ┌─────────────────────────────┐
    │       Handler Mapping       │
    │   (Registry / Strategy)    │
    └─────────────┬──────────────┘
                │
    ┌─────────────┴──────────────┐
    │          Validator          │
    │   (Strategy / Visitor)     │
    └─────────────┬──────────────┘
                │
    ┌─────────────┴──────────────┐
    │    HttpMessageConverter     │
    │        (Strategy)           │
    │ Request Body → Java Object │
    └─────────────┬──────────────┘
                │
                ▼
    ┌─────────────────────────────┐
    │        Controller           │
    │  (REST / Business Logic)    │
    └─────────────┬──────────────┘
                │
    ┌─────────────┴──────────────┐
    │    HttpMessageConverter     │
    │        (Strategy)           │
    │ Java Object → Response Body │
    └─────────────┬──────────────┘
                │
                ▼
    ┌─────────────────────────────┐
    │       HTTP Response         │
    └─────────────────────────────┘
