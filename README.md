# kt-brigadier

A Kotlin-first DSL and structured execution framework built on top of Mojang's Brigadier command library.

**kt-brigadier does not replace Brigadier** — it enhances it by preserving Brigadier's dispatcher, parsing engine, node
system, and execution model while transforming the development experience into a structured, middleware-enabled,
Kotlin-native command framework.

---

## Table of Contents

- [Introduction](#introduction)
    - [What is kt-brigadier?](#what-is-kt-brigadier)
    - [Why Use kt-brigadier?](#why-use-kt-brigadier)
    - [Core Features](#core-features)
- [Getting Started](#getting-started)
    - [Basic Command](#basic-command)
    - [Command Tree Structure](#command-tree-structure)
    - [Arguments](#arguments)
- [Type-Safe Argument Handling](#type-safe-argument-handling)
    - [Basic Argument Access](#basic-argument-access)
    - [Argument References](#argument-references)
- [Guards: The Middleware System](#guards-the-middleware-system)
    - [Understanding Guards](#understanding-guards)
    - [Basic Guard Usage](#basic-guard-usage)
    - [Argument Transformation](#argument-transformation)
    - [Mutable Command Context](#mutable-command-context)
- [Advanced Features](#advanced-features)
    - [Suggestions DSL](#suggestions-dsl)
    - [Access Control: requires vs Guards](#access-control-requires-vs-guards)
- [Architecture](#architecture)
    - [Execution Lifecycle](#execution-lifecycle)
    - [Design Principles](#design-principles)
- [Summary](#summary)

---

## Introduction

### What is kt-brigadier?

Brigadier is a powerful, performant, tree-based command parsing library. However, its API is low-level and verbose when
building complex command trees. kt-brigadier addresses this by providing a clean, idiomatic Kotlin interface while
maintaining 100% compatibility with Brigadier's core functionality.

You still use Brigadier's dispatcher and nodes — kt-brigadier simply makes defining them clean, structured, and
maintainable.

### Why Use kt-brigadier?

**Problems with Pure Brigadier:**

Working directly with Brigadier often leads to:

- Deep `.then()` nesting that obscures command structure
- Repetitive generic type declarations
- Manual `ctx.getArgument(...)` calls scattered throughout code
- Duplicated validation logic across command handlers
- No middleware pipeline for shared validation
- No structured approach to argument transformation

**Example in Pure Brigadier:**

```java
dispatcher.register(
        literal("group")
            .then(argument("name", StringArgumentType.word())
                .then(literal("info")
                    .executes(ctx -> {
                        String name = ctx . getArgument ("name", String.class);
                        // resolve group...
                        return 1;
                }))));
```

This becomes increasingly difficult to read and maintain as command trees grow.

### Core Features

kt-brigadier provides:

- **Clean Kotlin DSL** — Visually structured command definitions
- **Middleware Guards** — Centralized validation and transformation pipeline
- **Type-Safe Arguments** — Reified generics remove boilerplate
- **Argument References** — Strongly typed, reusable argument bindings
- **Transformation Pipeline** — Convert parsed strings to domain objects
- **Mutable Context** — Safe argument injection and override system
- **Suggestions DSL** — Simplified suggestion handling
- **Full Brigadier Compatibility** — Drop-in enhancement, not a replacement

---

## Getting Started

### Basic Command

```kotlin
val node = command<CommandSource>("hello") {
    execute {
        println("Hello world!")
        SINGLE_SUCCESS
    }
}
dispatcher.register(node)
```

This registers the `/hello` command.

### Command Tree Structure

The DSL mirrors your command structure visually, making the hierarchy immediately clear:

```kotlin
command<CommandSource>("group") {

    literal("info") {
        execute {
            println("Showing group info")
            SINGLE_SUCCESS
        }
    }

    literal("list") {
        execute {
            println("Listing groups")
            SINGLE_SUCCESS
        }
    }

    literal("create") {
        execute {
            println("Creating group")
            SINGLE_SUCCESS
        }
    }
}
```

**Benefits:**

- Indentation reflects tree hierarchy
- No `.then()` chains
- Clear parent-child relationships
- Easy to navigate and modify

### Arguments

```kotlin
command<CommandSource>("user") {

    argument("name", StringArgumentType.word()) {
        execute {
            val name = arg<String>("name")
            println("User: $name")
            SINGLE_SUCCESS
        }
    }
}
```

**What This Does:**

- Registers `/user <name>` command
- Parses `name` argument
- Provides type-safe access in execution block

---

## Type-Safe Argument Handling

### Basic Argument Access

**Instead of Brigadier's verbose approach:**

```java
ctx.getArgument("name",String .class)
```

**Use kt-brigadier's type-safe methods:**

```kotlin
arg<String>("name")         // Required argument
optionalArg<String>("name") // Optional argument
```

**Benefits:**

- No explicit `.class` parameter
- Leverages Kotlin's reified generics
- Cleaner, more maintainable code
- Compile-time type safety

### Argument References

#### The Problem

String-based argument access becomes fragile and error-prone in large command trees:

```kotlin
// Repeated throughout multiple handlers
arg<String>("group")
arg<String>("group")
arg<String>("group")
```

**Issues:**

- Typos cause runtime errors
- No IDE refactoring support
- Hard to track usage
- Difficult to maintain

#### The Solution: KtArgumentRef

Bind once, reuse everywhere with strong typing:

```kotlin
command<CommandSource>("group") {

    argument("group", StringArgumentType.word()) {
        val groupRef = argRef()

        literal("info") {
            execute {
                val group = groupRef.get()
                println("Group: $group")
                SINGLE_SUCCESS
            }
        }

        literal("delete") {
            execute {
                val group = groupRef.get()
                println("Deleting $group")
                SINGLE_SUCCESS
            }
        }

        literal("members") {
            execute {
                val group = groupRef.get()
                println("Members of $group")
                SINGLE_SUCCESS
            }
        }
    }
}
```

**How It Works:**

- `argRef()` creates a reference to the argument at the current scope
- The reference is bound to the `"group"` argument
- Multiple handlers can safely reuse the reference
- Type information is preserved

**Why This Matters:**

- **Strong Typing** — Compile-time safety
- **No String Keys** — Eliminate magic strings
- **Refactoring Support** — IDE can track references
- **Guard Integration** — Works seamlessly with middleware

---

## Guards: The Middleware System

Guards are one of the **core architectural features** of kt-brigadier. They introduce a structured middleware pipeline
between parsing and execution, enabling centralized validation and transformation.

### Understanding Guards

#### The Problem in Pure Brigadier

In vanilla Brigadier:

- Validation logic must be duplicated in every execute block
- Argument transformation must be repeated across handlers
- No mechanism exists for shared subtree validation
- Business logic and validation become tightly coupled
- Domain object resolution happens repeatedly

#### What Guards Provide

Guards create a middleware layer that:

- **Executes After Parsing** — Work with validated command structure
- **Executes Before Handlers** — Intercept before business logic runs
- **Execute Hierarchically** — Run from root → leaf in tree order
- **Can Abort Execution** — Prevent handler execution with custom result codes
- **Can Transform Arguments** — Convert parsed strings to domain objects
- **Can Inject Arguments** — Add new values to context
- **Can Override Values** — Replace parsed arguments with transformed ones

### Basic Guard Usage

```kotlin
command<CommandSource>("admin") {

    guard {
        if (!source.isAdmin()) {
            println("Access denied")
            abort() // Returns NO_SUCCESS by default
        } else {
            continueCommand()
        }
    }

    literal("reload") {
        execute {
            println("Reloaded")
            SINGLE_SUCCESS
        }
    }

    literal("shutdown") {
        execute {
            println("Shutting down")
            SINGLE_SUCCESS
        }
    }
}
```

**Flow:**

1. Command is parsed
2. Guard checks if user is admin
3. If not admin → `abort()` stops execution
4. If admin → `continueCommand()` allows execution to proceed
5. Execute block runs

**Note:** For simple permission checks, a `requires` predicate is often more appropriate as it affects node visibility.
Use guards when you need more complex logic or custom error handling.

### Argument Transformation

This is where Guards become particularly powerful.

#### Example Scenario: Group Resolution

**Command Structure:**

```
/group <group> info
/group <group> delete
/group <group> members add <user>
/group <group> members remove <user>
```

**Requirements:**

- Validate that `<group>` exists
- Convert string → Group domain object
- Inject the resolved Group into execution context
- Avoid duplicate resolution in every handler
- Handle "not found" cases consistently

#### Without Guards (Repetitive)

Every execute block must duplicate the resolution logic:

```kotlin
argument("group", StringArgumentType.word()) {

    literal("info") {
        execute {
            val name = arg<String>("group")
            val group = groupService.find(name)
            if (group == null) {
                println("Group not found: $name")
                return@execute NO_SUCCESS
            }
            println("Group info: $group")
            SINGLE_SUCCESS
        }
    }

    literal("delete") {
        execute {
            val name = arg<String>("group")
            val group = groupService.find(name)
            if (group == null) {
                println("Group not found: $name")
                return@execute NO_SUCCESS
            }
            println("Deleting group: $group")
            SINGLE_SUCCESS
        }
    }
    
    // and so on...
}
```

**Problems:**

- Resolution logic duplicated in every handler
- Inconsistent error messages
- Hard to modify validation rules
- Performance overhead from repeated lookups
- Business logic cluttered with validation

#### With Guards (Centralized)

```kotlin
argument("group", StringArgumentType.word()) {
    val groupRef = argRef()

    guard {
        val name = groupRef.get()
        val group = groupService.find(name)

        if (group == null) {
            println("Group not found: $name")
            return@guard abort(NO_SUCCESS)
        }

        // Transform string to domain object
        setArgument("group", group)
        continueCommand()
    }

    literal("info") {
        execute {
            val group = arg<Group>("group") // Now receives Group object
            println("Group info: ${group.name} (${group.members.size} members)")
            SINGLE_SUCCESS
        }
    }

    literal("delete") {
        execute {
            val group = arg<Group>("group") // Same domain object
            groupService.delete(group)
            println("Deleted group: ${group.name}")
            SINGLE_SUCCESS
        }
    }

    literal("members") {
        // Nested commands also receive the transformed Group
        literal("list") {
            execute {
                val group = arg<Group>("group")
                println("Members: ${group.members.joinToString()}")
                SINGLE_SUCCESS
            }
        }
    }
}
```

**Benefits:**

- ✅ Resolution happens exactly once
- ✅ Validation is centralized and consistent
- ✅ Execution logic works with domain objects
- ✅ Business logic is clean and focused
- ✅ Error handling is unified
- ✅ Performance improved (single lookup)
- ✅ Easy to modify validation rules

### Mutable Command Context

Guards operate on `KtCommandContext`, which extends Brigadier's `CommandContext` with safe mutation capabilities.

#### Available Operations

```kotlin
// Override or inject an argument
setArgument(name: String, value: Any)

// Remove an argument completely (further accessing will throw an exception)
removeArgument(name: String)

// Reset argument to original parsed value
resetArgument(name: String)
```

#### Precedence Rules

Overridden arguments **always take precedence** over parsed ones:

```kotlin
guard {
    val raw = arg<String>("count")  // "5"
    val parsed = raw.toInt()        // 5

    setArgument("count", parsed)    // Override with Integer
    continueCommand()
}

execute {
    val count = arg<Int>("count")   // Receives Integer, not String
}
```

This enables safe, predictable transformation pipelines.
Of course, you should use IntegerArgumentType instead of StringArgumentType for numeric arguments.

---

## Advanced Features

### Suggestions DSL

Brigadier suggestions normally require manual `CompletableFuture<Suggestions>` handling. kt-brigadier simplifies this
dramatically.

#### Simple Suggestions

```kotlin
argument("color", StringArgumentType.word()) {
    suggests {
        suggest("red")
        suggest("green")
        suggest("blue")
    }
}
```

**Notes:**

- `suggests` is called every time the argument needs completion
- You can use dynamic values based on current context
- Access `CommandContext` via `context` property
- Access nativ `SuggestionsBuilder` via `builder` property
- **Guards do not run during suggestion** — only during execution

#### Suggestions with Tooltips

```kotlin
argument("permission", StringArgumentType.word()) {
    suggests {
        suggest("admin", LiteralMessage("Full administrative access"))
        suggest("moderator", LiteralMessage("Moderation permissions"))
        suggest("user", LiteralMessage("Standard user permissions"))
    }
}
```

#### Dynamic Suggestions

```kotlin
argument("player", StringArgumentType.word()) {
    suggests {
        val online = server.getOnlinePlayers()
        online.forEach { player ->
            suggest(player.name)
        }
    }
}
```

#### Full Control Override

For advanced cases where you need complete control:

```kotlin
argument("custom", StringArgumentType.word()) {
    suggestsOverride { builder ->
        // Full CompletableFuture access
        builder.suggest("dynamicValue")
        builder.buildFuture()
    }
}
```

### Access Control: requires vs Guards

These two features operate at different architectural layers and serve complementary purposes.

#### requires (Brigadier Native)

**Purpose:** Control structural accessibility during parsing

**Characteristics:**

- ✅ Runs during command parsing phase
- ✅ Controls node visibility
- ✅ Affects command tree structure
- ✅ Fast (pre-execution)
- ❌ Cannot access arguments
- ❌ Cannot modify context
- ❌ Cannot return custom result codes
- ❌ Boolean only (allow/deny)

**Example:**

```kotlin
command<CommandSource>("admin") {
    requires { hasPermission("admin") }

    literal("reload") {
        execute {
            println("Reloading...")
            SINGLE_SUCCESS
        }
    }
}
```

**When to Use:**

- Simple permission checks
- Static access control
- When node visibility matters
- When you need Brigadier's built-in permission system

#### Guards (kt-brigadier Feature)

**Purpose:** Validate, transform, and prepare for execution

**Characteristics:**

- ✅ Runs after successful parsing
- ✅ Full argument access
- ✅ Can mutate context
- ✅ Can inject new arguments
- ✅ Can transform arguments
- ✅ Can abort with custom result codes
- ✅ Shared subtree validation logic
- ✅ Domain object transformation
- ❌ Does not affect node visibility
- ❌ Runs after parsing

**Example:**

```kotlin
argument("group", StringArgumentType.word()) {
    guard {
        val name = arg<String>("group")
        val group = groupService.find(name)

        if (group == null) {
            println("Group not found: $name")
            return@guard abort()
        }

        if (!group.hasPermission(source, "view")) {
            println("Access denied")
            return@guard abort(NO_SUCCESS)
        }

        setArgument("group", group)
        continueCommand()
    }

    // Handlers receive transformed Group object
}
```

**When to Use:**

- Argument validation
- Domain object resolution
- Complex permission checks requiring context
- Argument transformation
- Shared validation across subtree
- Custom error handling

#### Comparison Table

| Feature                  | requires | Guard |
|--------------------------|----------|-------|
| Parsing phase            | ✓        | ✗     |
| Post-parse validation    | ✗        | ✓     |
| Access arguments         | ✗        | ✓     |
| Modify context           | ✗        | ✓     |
| Argument transformation  | ✗        | ✓     |
| Argument injection       | ✗        | ✓     |
| Custom result codes      | ✗        | ✓     |
| Shared subtree logic     | ✗        | ✓     |
| Affects node visibility  | ✓        | ✗     |
| Domain object resolution | ✗        | ✓     |

#### Using Both Together

```kotlin
command<CommandSource>("group-admin") {
    requires { hasPermission("admin.access") }  // Node-level access control

    argument("group", StringArgumentType.word()) {
        guard {
            // Additional validation with context
            val name = arg<String>("group")
            val group = groupService.find(name)

            if (group == null) {
                println("Group not found: $name")
                return@guard abort(NO_SUCCESS)
            }

            // Check if admin has permission for THIS specific group
            if (!group.allowsAdmin(source)) {
                println("Access denied")
                return@guard abort(NO_SUCCESS)
            }

            setArgument("group", group)
            continueCommand()
        }

        literal("delete") {
            requires { hasPermission("admin.group.delete") }  // More specific permission
            execute {
                val group = arg<Group>("group")
                groupService.delete(group)
                SINGLE_SUCCESS
            }
        }
    }
}
```

**Summary:** They complement each other, not replace each other. Use `requires` for static access control and node
visibility, use Guards for dynamic validation and transformation.

---

## Architecture

### Execution Lifecycle

When a command is executed, the following happens in order:

```
1. User Input
   ↓
2. Brigadier Parsing
   ↓
3. requires Predicates (if any)
   ↓
4. Guards Execute (root → leaf order)
   ↓
   ├─ Guard 1 at root
   ├─ Guard 2 at intermediate node
   └─ Guard 3 at leaf node
   ↓
5. All Guards returned GuardResult.Continue?
   ├─ YES → Execute Handler
   └─ NO  → Stop (return Guard's abort result)
   ↓
6. Return Result
```

**Clear Separation of Concerns:**

- **Parsing** → Brigadier handles syntax and structure
- **Access Control** → `requires` handles node-level permissions
- **Validation & Transformation** → Guards handle business validation
- **Business Logic** → `execute` handles core functionality

### Design Principles

kt-brigadier is built on strict architectural decisions:

1. **Non-Invasive Architecture**
    - Brigadier remains completely untouched
    - No modifications to core parsing logic
    - Full backward compatibility

2. **Middleware-First Execution Model**
    - Guards create a structured pipeline
    - Clear separation between validation and execution
    - Predictable execution order

3. **Explicit Control Flow**
    - All Guard results are explicit (`abort()` or `continueCommand()`)
    - No implicit behavior or magic
    - Clear intent in code

4. **Domain-Driven Transformation**
    - Support converting parsed primitives to domain objects
    - Centralized transformation logic
    - Type-safe argument access

5. **Kotlin-Native DSL**
    - Idiomatic Kotlin syntax
    - Leverage language features (reified generics, lambdas, etc.)
    - Clean, readable command definitions

6. **No Hidden Magic**
    - All behavior is explicit
    - No reflection-based surprises
    - What you see is what you get

7. **Full Brigadier Compatibility**
    - Works with existing Brigadier code
    - Can be adopted incrementally
    - Integrates with Brigadier ecosystem

---

## Summary

kt-brigadier transforms Brigadier from a low-level parsing API into a structured, middleware-enabled command framework
that embraces Kotlin idioms while preserving Brigadier's power and performance.

### Key Benefits

✅ **Clean DSL Structure** — Command trees that match your mental model  
✅ **Type-Safe Arguments** — Reified generics eliminate boilerplate  
✅ **Argument References** — Strongly-typed, reusable bindings  
✅ **Middleware Guards** — Centralized validation and transformation  
✅ **Domain Transformation** — Work with domain objects, not primitives  
✅ **Mutable Context** — Safe argument injection and override  
✅ **Suggestions DSL** — Simplified completion handling  
✅ **Full Compatibility** — Drop-in **enhancement** for Brigadier

---

**Author:** Fantamomo  
**License:** [MIT](LICENSE)