# DDD Studio booking

TODO pwestlin: Document

## Package structure
```
com.example.studiobooking/
├── domain/                  <-- Ren Kotlin (Noll Spring-dependencies)
│   ├── model/
│   │   ├── Pass.kt          <-- Aggregate Root
│   │   ├── PassId.kt        <-- Value Object
│   │   └── Slot.kt          <-- Value Object
│   ├── event/
│   │   └── PassBookedEvent.kt
│   └── repository/
│       └── PassRepository.kt <-- Interface (Port)
├── application/             <-- Orkestrering & Use Cases
│   └── BookPassUseCase.kt   <-- Använder @Service / @Transactional här!
└── infrastructure/          <-- Ramverk, DB & API
    ├── persistence/         <-- JPA Entities, Spring Data Repositories, Mappers
    └── rest/                <-- REST-controllers & DTOs
```