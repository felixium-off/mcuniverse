# Architecture Design Review

**Author**: Senior Game Developer (AI Assistant)
**Date**: 2026-02-05
**Project**: MCUniverse

## Executive Summary
The current architecture demonstrates a solid foundation with modular features (`GameFeature`) and strategy patterns. However, structural inconsistencies in the persistence layer and tight coupling in the initialization phase (`Main.java`) pose scalability risks.

This document outlines key observations and provides a refactoring roadmap to align with industry standards such as Domain-Driven Design (DDD) and SOLID principles.

## 1. Architectural Observations

### ✅ Strengths
- **Modular Feature Design**: The `GameFeature` interface is an excellent abstraction. It allows for easy enabling/disabling of game modules.
- **Strategy Pattern Usage**: Using `EconomyStrategy` allows switching implementations (e.g., specific DBs) without affecting the service layer.
- **Modern Command Framework**: Integration with `Lamp` suggests a forward-thinking approach to command handling.

### ⚠️ Critical Findings

#### A. Inconsistent Persistence Layer (Strategy vs. Repository)
- **Economy**: Uses `EconomyStrategy` which mixes **business logic** (e.g., `withdraw` which implies validation) with **data access**.
- **Cosmetics**: Uses `CosmeticRepository` which correctly focuses purely on CRUD operations (`load`/`save`).
- **Risk**: "Strategy" usually implies algorithmic variance, while "Repository" implies data access. Mixing these confuses developers.

#### B. Initialization "Spaghetti" in Main
- `Main.java` manually instantiates `RankFeature`, calls `enable()` (partially), then passes it to `LampFactory`, effectively hardcoding the initialization order.
- **Risk**: As modules grow, this manual dependency wiring will become fragile and error-prone.

#### C. Anemic Domain Models
- Objects like `Warp` (Record) and `Rank` (Enum) appear to be data holders.
- **Risk**: Business logic tends to leak into "Service" classes, creating "God Services" rather than having rich, self-validating domain objects.

---

## 2. Recommendations

### R1. Standardize Persistence Layer (Repository Pattern)
Rename and refactor `EconomyStrategy` to separate logic from data.

**Current:**
```java
// EconomyStrategy.java
boolean withdraw(UUID uuid, AccountType type, long amount); // Logic + Data
```

**Proposed:**
```java
// EconomyRepository.java
EconomyData findById(UUID uuid);
void save(EconomyData data);

// EconomyService.java
public void withdraw(...) {
    EconomyData data = repository.findById(uuid);
    data.withdraw(amount); // Logic stays in Domain Object
    repository.save(data);
}
```

### R2. Introduce Dependency Injection (DI)
Stop manually wiring services in `Main.java`. Use a Service Locator or a proper DI framework (like Guice, or even a simple custom Registry).

**Proposed Pattern:**
```java
// FeatureRegistry.java
public void register(GameFeature feature) { ... }
public <T> T getService(Class<T> serviceClass) { ... }
```
This allows `RankFeature` to register itself, and `LampFactory` to ask the registry for the `RankService` without `Main` managing the variable directly.

### R3. Enforce "Rich Domain Model"
Move logic *into* the objects.
- **Rank**: Should have methods like `boolean includes(Rank other)`.
- **Warp**: Should implement `teleport(Player player)` (encapsulating the logic of *how* to teleport).

### R4. Asynchronous I/O Enforcement
Ensure calls like `EconomyStrategy.getAccount` (which likely hits MongoDB) are **never** called on the main server thread.
- Change return types to `CompletableFuture<Long>` or similar async primitives to force async handling at the API level.

## 3. Immediate Action Plan

1.  **Refactor Economy**: Split `EconomyStrategy` into `EconomyService` (Business Rules) and `EconomyRepository` (Data Access).
2.  **Clean Main**: Create a `FeatureLoader` class to handle the registration and sorting of features based on `@Dependency` annotations (if complex) or simple phase ordering.
3.  **Async Audit**: Review all DatabaseManager usages to ensure `Block` calls aren't happening on the Tick loop.
