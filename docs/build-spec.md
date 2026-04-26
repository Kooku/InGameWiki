# Build Spec

## Purpose

This document locks the initial technical choices for the first playable `InGameWiki` prototype so implementation can begin without reopening baseline setup decisions.

## Target Platform

- Minecraft: `1.21.10`
- Loader: Fabric
- Environment: client-side only

## Java Target

- Java: `21`
- Do not add extra compatibility work for older Java runtimes in the MVP.

Implementation note:

- The Gradle build now explicitly targets Java 21.

## Naming

- Base package: `com.ingamewiki`
- Mod ID: `ingamewiki`
- Main UI surface: `InGameWiki`

## Content Loading Strategy

### MVP Behavior

- Load bundled wiki articles at client startup.
- Build the initial in-memory repository and search index before the player opens the UI.

### Deferred Optimization

- Do not implement lazy loading in the MVP.
- If content volume later makes startup or memory usage meaningfully worse, revisit:
  - loading articles on first UI open
  - loading article bodies on demand
  - rebuilding the search index incrementally

Current decision:

- optimize later only if content growth proves it necessary

## Packaging Assumptions

- Articles ship inside mod resources
- Article format is JSON
- No remote fetches
- No browser dependency

## First Implementation Targets

The first implementation pass should produce:

1. a runnable Fabric mod project for `1.21.10`
2. a pause-menu entry into `InGameWiki`
3. a working screen with sample data
4. bundled article loading at startup
5. simple alias-aware search

## Current Scaffold Versions

The current scaffold is pinned to:

- Fabric Loader: `0.19.2`
- Fabric API: `0.138.4+1.21.10`
- Fabric Loom: `1.16.1`
- Gradle Wrapper: `9.4.1`

## Explicit Non-Goals

- multi-version support
- server-side compatibility layers
- runtime content syncing
- premature content-loading optimization
