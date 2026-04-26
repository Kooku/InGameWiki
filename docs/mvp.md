# MVP Spec

## Summary

`InGameWiki` is a manual, in-game knowledge panel for vanilla Minecraft. It should answer common survival questions in seconds and work on Realms because it is fully client-side.

The first release should optimize for:

- speed
- clarity
- relevance
- low annoyance

## Primary User

A vanilla or lightly-modded player who is on a Realm or server and does not want to alt-tab for common knowledge lookups.

## Core UX

1. Player pauses the game.
2. Player opens `InGameWiki`.
3. Player searches a simple term such as `villager`, `mending`, `diamonds`, `slime`, or `netherite`.
4. The mod shows a short answer immediately.
5. The player can jump to related topics if needed.

## V1 Scope

- Manual access only
- Search UI
- Curated local article data
- Version-aware article labels
- Short article format
- Related topic links

## Out of Scope

- Contextual trigger system
- Hover hints
- Browser integration
- Remote content fetching
- Community editing
- Full-text article renderer

## Article Format

Each article should fit roughly one screen and use the same structure:

### Quick Answer

The direct answer the player most likely wants.

### Key Facts

A short list of rules, values, or constraints.

### Common Mistakes

Misconceptions or actions that waste time.

### Related Topics

Links to 2 to 5 relevant articles.

## Initial Topic Set

These are good candidates for the first article batch.

### Villagers

- villager basics
- professions
- workstations
- restocking
- librarian and mending
- curing discounts
- breeding villagers

### Mining and Resources

- diamonds
- iron
- coal
- redstone
- lapis
- gold
- netherite and ancient debris
- slime chunks

### Survival Systems

- enchanting
- bookshelves
- anvils
- raids
- bad omen
- brewing basics
- beacon basics

### Dimensions and Progression

- Nether progression
- Eyes of Ender
- End portal strongholds
- elytra and End cities

## Search Requirements

Search should work on intent, not exact page names.

Examples:

- `diamond y level` -> `diamonds`
- `mending villager` -> `librarian and mending`
- `slimes` -> `slime chunks`
- `how villagers restock` -> `restocking`
- `ancient debris` -> `netherite and ancient debris`

V1 search can be simple keyword scoring backed by curated aliases.

## Content Rules

- Prefer practical guidance over exhaustive detail.
- Avoid walls of text.
- Keep terminology familiar to players.
- Mark version-sensitive facts clearly.
- Do not include information that depends on server plugins or mods.

## V1 Success Criteria

- A player can answer a common vanilla question without leaving the game.
- A typical article is readable in under 20 seconds.
- Search returns a useful top result for obvious player phrasing.
- The UI feels like a tool, not a tutorial system.

## Likely Next Step After MVP

Once manual lookup is solid, the first optional enhancement is a lightweight contextual entry point, such as:

- hover indicator for supported entities or blocks
- `Hold H` to search the currently targeted thing

That should only happen after the manual flow is clearly useful on its own.
