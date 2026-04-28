# InGameWiki

`InGameWiki` is a client-side Minecraft mod concept for fast, in-game help.

The core problem is simple: players on vanilla servers and Realms still alt-tab for common questions like:

- How do villager professions and restocking work?
- What Y level should I mine at for a resource?
- Which villager can sell Mending?
- How do slime chunks, raids, or curing discounts work?

The goal is not to mirror the full Minecraft Wiki. The goal is to answer practical questions in one screen, inside the game, without a browser handoff.

## Product Direction

- Client-side only
- Realms-safe
- Manual lookup first
- Pack-driven
- Short, curated answers instead of long-form wiki pages

## MVP

Version 1 is intentionally narrow:

- `Esc -> InGameWiki`
- Search by plain language
- Curated article set
- No popups
- No passive triggers
- No browser redirect

See [docs/mvp.md](docs/mvp.md) for the working MVP spec.
See [docs/implementation-plan.md](docs/implementation-plan.md) for the implementation phases, current architecture, and handoff plan.
See [docs/build-spec.md](docs/build-spec.md) for the locked technical setup choices for the first prototype.

## Non-Goals

- Replacing the full Minecraft Wiki
- Server-side integration
- Live web sync
- Auto-detected contextual hints in v1
- Multi-pack runtime support in v1

## Current Status

The repository now contains a working Fabric scaffold for `1.21.10`:

- Java 21 build setup
- pause-menu `InGameWiki` entry point
- two-pane in-game screen with an empty-search home state
- bundled content-pack and article JSON loading
- basic alias-aware search
- failure handling for broken or missing wiki content

This repo currently ships the vanilla pack as the reference implementation. The next implementation steps are expanding that article set and improving search coverage.
