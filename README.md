# Autumn Framework

Autumn is a lightweight Java dependency injection (DI) and application framework inspired by Spring Boot. It provides a component-based architecture with automatic bean management, lifecycle hooks, and a simple package-scanning mechanism.


# Not Bad, Hmm?


┌─────────────────────────────┐
│       Application.main()     │
│  AutumnApplicationRunner.run│
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│  AutumnApplicationContext   │
│  - Scans base package        │
│  - Creates beans             │
│  - Registers EventListeners  │
│  - Handles Conditional beans │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│  Beans with @Component       │
│  are instantiated            │
│  - DispatcherAutoConfiguration │
│    is one of these beans      │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ DispatcherAutoConfiguration │
│  - @PostConstruct runs       │
│  - Creates MiniDispatcher    │
│  - Scans all @RestController │
│    beans                     │
│  - Registers controllers     │
│  - Adds default converters   │
│  - Starts MiniDispatcher on 8080 │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│      MiniDispatcher          │
│  - Listens on port 8080      │
│  - Handles incoming HTTP     │
│    requests                  │
│  - Matches request to        │
│    HandlerMethod             │
│  - Applies filters & converters│
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│    HandlerMethod (Controller)│
│  - Executes the annotated    │
│    method (@GetMapping, etc.)│
│  - Returns response          │
└─────────────────────────────┘


