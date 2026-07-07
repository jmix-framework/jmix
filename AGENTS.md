# Jmix — Agent Guidance

## Project Structure & Module Organization

- Multi-module Gradle workspace; standard `src/main` / `src/test` layout per module.
- `jmix-bom` — BOM for shared dependencies and Jmix modules.
- `jmix-gradle-plugin` — the Gradle plugin used by applications built on Jmix.
- `jmix-build` — the Gradle plugin used to build Jmix itself.

## Build and Test Commands

- `./gradlew build` — build all modules and run checks.
- `./gradlew test` — run tests across modules.
- `./gradlew :<module-name>:test` — test a single module by its leaf project name from `settings.gradle` (e.g. `:data:test` for the `jmix-data/data` module, not `:jmix-data:data:test`).
- `./gradlew :sample-rest:test` — the REST API test suite (`jmix-rest/sample-rest`).

## Generated sources

- Do not read `JPA2Lexer.java` / `JPA2Parser.java` in full — they are generated from `JPA2.g` / `JPA2.tokens` (see `jmix-data/data/src/main/java/io/jmix/data/impl/jpql/antlr2/README.md`).
