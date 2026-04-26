# InGameWiki

`InGameWiki` is a client-side Minecraft mod concept for fast, in-game vanilla help.

The core problem is simple: players on vanilla servers and Realms still alt-tab for common questions like:

- How do villager professions and restocking work?
- What Y level should I mine at for a resource?
- Which villager can sell Mending?
- How do slime chunks, raids, or curing discounts work?

The goal is not to mirror the full Minecraft Wiki. The goal is to answer practical survival questions in one screen, inside the game, without a browser handoff.

## Product Direction

- Client-side only
- Realms-safe
- Manual lookup first
- Vanilla-focused
- Short, curated answers instead of long-form wiki pages

## MVP

Version 1 is intentionally narrow:

- `Esc -> InGameWiki`
- Search by plain language
- Curated article set
- No popups
- No passive triggers
- No browser redirect

See [docs/mvp.md](/Users/kuyoungshin/Coding/InGameWiki/docs/mvp.md) for the working MVP spec.
See [docs/implementation-plan.md](/Users/kuyoungshin/Coding/InGameWiki/docs/implementation-plan.md) for the implementation phases, architecture, and handoff plan.
See [docs/build-spec.md](/Users/kuyoungshin/Coding/InGameWiki/docs/build-spec.md) for the locked technical setup choices for the first prototype.

## Non-Goals

- Replacing the full Minecraft Wiki
- Server-side integration
- Live web sync
- Auto-detected contextual hints in v1
- Modded content support in v1

## Current Status

The repository now contains a working Fabric scaffold for `1.21.10`:

- Java 21 build setup
- pause-menu `InGameWiki` entry point
- starter in-game screen
- bundled article JSON loading
- basic alias-aware search

The next implementation step is refining the UI and expanding the article set.
