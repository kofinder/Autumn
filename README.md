# ⚙️ Autumn

### Disclaimer
This is just an experiment to see what I can build in Java. Don’t take it too seriously — and please don’t blame me if something breaks. 😅  

**Autumn** is a lightweight Java dependency injection (DI) and application framework inspired by Spring Boot.  
I was simply curious about how Spring works under the hood, so I tried to recreate some of its ideas.  

It currently provides:  
- A component-based architecture  
- Automatic bean management  
- Lifecycle hooks  
- Simple package scanning and registration mechanism  

---

### 🧩 How It Works

```
┌─────────────────────────────┐
│      Application.main()     │
│  → AutumnApplicationRunner.run │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│   AutumnApplicationContext  │
│   - Scans base package      │
│   - Creates beans           │
│   - Registers EventListeners│
│   - Handles conditional beans│
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│   @Component Beans           │
│   - Instantiated automatically│
│   - Includes                  │
│     DispatcherAutoConfiguration │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ DispatcherAutoConfiguration │
│  - Runs @PostConstruct       │
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
│        MiniDispatcher        │
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
│  HandlerMethod (Controller)  │
│  - Executes annotated methods│
│    (@GetMapping, etc.)       │
│  - Returns response           │
└─────────────────────────────┘
```

---

### 🧠 Notes
This project is purely for learning and experimentation.  
If you’re curious about how frameworks like Spring work behind the scenes, *Autumn* is a fun playground to explore those ideas from scratch.  
