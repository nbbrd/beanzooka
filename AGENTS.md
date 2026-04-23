# AGENTS.md — NetBeans Platform Launcher

## Overview

**Beanzooka** is a Swing desktop tool for launching multiple simultaneous sessions of a [NetBeans Platform](https://netbeans.apache.org/front/main/projects/platform/) application, each with its own isolated configuration.

Key capabilities:
- Launch several sessions of a NetBeans Platform application side by side
- Configure each session independently: JDK, user directory, and plugins
- Compare different application versions, workspaces, JDKs, or plugin combinations
- Persist resource sets to XML files for repeatable scenarios
- Accept a resource file as a command-line argument for automation
- Licensed under [EUPL](https://joinup.ec.europa.eu/page/eupl-text-11-12); maintained by the [National Bank of Belgium](https://www.nbb.be)

## Architecture

The project is a single-module Maven build organized in three packages:

### 1. Core (`beanzooka.core`)

Domain model and session-management logic. No UI dependencies.

| Class           | Purpose                                                                                                                          |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------|
| `App`           | Represents a NetBeans Platform executable (label + file path); detects platform executables via desktop search                   |
| `Jdk`           | Represents a JDK installation (label, `javaHome`, optional JVM options, extra clusters); writes the NetBeans `.conf` config file |
| `UserDir`       | Represents a NetBeans user directory (fixed, temporary, or cloned); creates the working directory before launch                  |
| `Plugin`        | Represents a `.nbm` plugin archive; extracts its content into the working directory                                              |
| `Resources`     | Aggregates all four resource lists (`App`, `Jdk`, `UserDir`, `Plugin`) as a single value object                                  |
| `Configuration` | Combines one `App`, one `Jdk`, one optional `UserDir`, and a list of `Plugin`s; orchestrates `init()` + `launch()`               |
| `Util`          | Internal file-copy helpers                                                                                                       |

### 2. IO (`beanzooka.io`)

Serialization/deserialization of the resource model.

| Class          | Purpose                                                                     |
|----------------|-----------------------------------------------------------------------------|
| `XmlResources` | StAX-based XML parser and formatter for `Resources` (round-trip read/write) |

### 3. Swing (`beanzooka.swing`)

Swing UI built with FlatLaf and `ec.util.various.swing`.

| Class / Form     | Purpose                                                                                                 |
|------------------|---------------------------------------------------------------------------------------------------------|
| `MainPanel`      | Main application window: resource panels, session table, toolbar actions (launch `F5`, open files/logs) |
| `ResourcesPanel` | Tab panel managing all four resource lists (add, edit, remove, auto-detect)                             |
| `SessionsPanel`  | Table of running/stopped sessions with context menu (open user dir / log)                               |
| `Session`        | Runtime state for a launched session (configuration, working dir, process)                              |
| `Renderers`      | Custom cell renderers for the resource and session tables                                               |

**Entry point**: `Beanzooka.main()` bootstraps the look-and-feel and opens `MainPanel`, optionally preloading a resource XML file passed as the first command-line argument.

## Build & Test

```shell
mvn clean install                 # full build + tests + enforcer checks
mvn clean install -Pyolo          # skip all checks (fast local iteration)
mvn test -pl <module-name> -Pyolo # fast test a single module
mvn test -pl <module-name> -am    # full test a single module
```

- **Java 8 target** with JPMS `module-info.java` compiled separately on JDK 9+ (see `java8-with-jpms` profile in root POM)
- **JUnit 5** with parallel execution enabled (`junit.jupiter.execution.parallel.enabled=true`); **AssertJ** for assertions

## Key Conventions

- **Lombok**: use lombok annotations when possible. Config in `lombok.config`: `addNullAnnotations=jspecify`, `builder.className=Builder`
- **Nullability**: `@org.jspecify.annotations.Nullable` for nullable; `@lombok.NonNull` for non-null parameters. Return types use `@Nullable` or the `OrNull` suffix (e.g., `getThingOrNull`)
- **Design annotations** use annotations from `java-design-util` such as `@VisibleForTesting`, `@StaticFactoryMethod`, `@DirectImpl`, `@MightBeGenerated`, `@MightBePromoted`
- **Internal packages**: `internal.<project>.*` are implementation details; public API lives in the root and `spi` packages
- **Static analysis**: `forbiddenapis` (no `jdk-unsafe`, `jdk-deprecated`, `jdk-internal`, `jdk-non-portable`, `jdk-reflection`), `modernizer`
- **Reproducible builds**: `project.build.outputTimestamp` is set in the root POM
- **Formatting/style**: 
  - Use IntelliJ IDEA default code style for Java
  - Follow existing formatting and match naming conventions exactly
  - Follow the principles of "Effective Java"
  - Follow the principles of "Clean Code"
- **Java/JVM**: 
  - Target version defined in root POM properties; some modules may require higher versions
  - Use modern Java feature compatible with defined version

## Agent behavior

- Do respect existing architecture, coding style, and conventions
- Do prefer minimal, reviewable changes
- Do preserve backward compatibility
- Do not introduce new dependencies without justification
- Do not rewrite large sections for cleanliness
- Do not reformat code
- Do not propose additional features or changes beyond the scope of the task
