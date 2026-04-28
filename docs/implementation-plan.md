# Implementation Plan

## Goal

Build the first usable version of `InGameWiki` as a Fabric client-side mod and reusable wiki engine for Minecraft players on Realms and servers.

The first version should let a player:

1. open `InGameWiki` from the pause menu
2. search a topic in plain language
3. read a concise in-game answer
4. follow related topics without leaving the game

## Scope Boundary

This plan covers the first playable prototype and MVP.

Included:

- Fabric project setup
- pause-menu entry point
- `InGameWiki` screen
- local content-pack and article data
- manual search
- article rendering
- starter vanilla content set

Excluded:

- contextual triggers
- hover hints
- browser handoff
- remote data fetching
- server-side features
- multi-pack runtime switching

## Implementation Principles

- Keep the mod fully client-side.
- Prefer static local data over runtime integration.
- Make the first screen useful before making it smart.
- Keep data and UI loosely coupled so content can grow without UI rewrites.
- Optimize for simple failure modes: if content fails to load, the mod should still open and show a clear fallback state.

## Technical Direction

### Loader

- Fabric

### Runtime Target

- Minecraft `1.21.10` for the first prototype
- No multi-version support in the initial implementation

### Base Package

- `com.ingamewiki`

### Java Target

- Java 21

### Data Strategy

- Ship curated content packs inside the mod resources
- Treat the current vanilla pack as the reference implementation for future forks
- Use JSON for article definitions
- Use a pack manifest so UI copy, examples, and article roots are pack-owned data
- Build a lightweight in-memory search view at client startup for the MVP
- Revisit lazy loading only if startup or content scale becomes a real problem

### UI Strategy

- Use vanilla screen widgets
- Add one pause-menu button to open `InGameWiki`
- Use a two-pane layout:
  - left: search box and results
  - right: home state or selected article

## Current Project Structure

This reflects the code that exists today.

```text
src/client/java/com/ingamewiki/client/
  InGameWikiClient.java
  PauseMenuIntegration.java
  ui/
    SmallWikiScreen.java

src/main/java/com/ingamewiki/
  InGameWikiMod.java
  content/
    Article.java
    ContentPack.java
    ContentPackLoader.java
    ArticleRepository.java
  search/
    SearchService.java

src/main/resources/
  fabric.mod.json
  assets/ingamewiki/
    icon.png
    lang/en_us.json
  data/ingamewiki/
    pack.json
    packs/vanilla/
      articles/
        index.json
        *.json
```

## Current Implementation Status

Completed:

- Fabric `1.21.10` scaffold
- Java 21 build setup
- client entrypoint and mod metadata
- pause-menu `InGameWiki` button
- `SmallWikiScreen` two-pane UI
- bundled content-pack loading at client startup
- fail-open handling for broken individual articles
- fail-close handling for global content load failure
- alias-aware search
- article rendering
- related-topic navigation
- empty-search home state

Not yet implemented:

- broader starter article set
- stronger search coverage and ranking tuning
- bookmarks
- featured/common topics
- contextual entry points such as hover hints or `Hold H`

## Content Model

Each article is a small, strongly typed unit rather than raw markdown.

Current fields:

- `id`
- `title`
- `aliases`
- `keywords`
- `category`
- `version_note`
- `quick_answer`
- `key_facts`
- `common_mistakes`
- `related_topics`

Example shape:

```json
{
  "id": "villager-restocking",
  "title": "Restocking",
  "aliases": ["villager restock", "villagers restock", "how villagers restock"],
  "keywords": ["trading hall", "profession block", "workstation access"],
  "category": "villagers",
  "version_note": "Java Edition 1.21.x",
  "quick_answer": "Villagers can restock up to twice per day if they can reach their workstation.",
  "key_facts": [
    "A villager must have a valid profession and workstation.",
    "A locked trade does not prevent future restocks.",
    "Breaking pathing to the workstation stops reliable restocking."
  ],
  "common_mistakes": [
    "Expecting a villager to restock without workstation access.",
    "Trying to reroll a villager after trading with it."
  ],
  "related_topics": ["villager-professions", "librarian-mending", "villager-workstations"]
}
```

Pack-owned metadata now lives in `data/ingamewiki/pack.json`. That manifest controls the pack identity, home-state copy, search placeholder text, and the article index root.

## Search Plan

V1 search should stay simple and deterministic.

Ranking inputs:

- exact title match
- exact alias match
- keyword overlap
- token overlap with title
- token overlap with aliases
- optional keyword overlap with article body fields

Search rules:

- normalize to lowercase
- ignore punctuation
- singular/plural tolerance where possible
- return a useful top result for obvious player phrasing
- keep ranking explainable and easy to tune

Non-goals:

- fuzzy ML ranking
- embeddings
- external search libraries unless the current implementation proves insufficient

## UI Plan

### Entry Point

- Add an `InGameWiki` button to the pause screen
- Open the main screen without closing the current world/session
- Preserve the expected location of `Save and Quit to Title`

### Main Screen

Current elements:

- search text box
- result list
- home state when no search is active
- article title
- version label if applicable
- `Quick Answer`
- `Key Facts`
- `Common Mistakes`
- `Related Topics`

Later polish candidates:

- bookmarks
- featured/common topics
- keyboard shortcut hints

### Empty and Failure States

The current UX should explicitly support:

- no query entered
- no results found
- broken individual article
- global content load failure
- article selected but missing related topic

## Phased Delivery

### Phase 1: Fabric Foundation

Status: completed

Delivered:

- Gradle and Fabric project setup
- client entrypoint
- mod metadata
- working local run/build flow

### Phase 2: Pause Menu Integration

Status: completed

Delivered:

- pause menu button
- screen open/close wiring
- placement that preserves the bottom-most exit action

### Phase 3: Screen Skeleton

Status: completed

Delivered:

- `SmallWikiScreen`
- search input
- result list
- article panel

### Phase 4: Content Loading and Failure Handling

Status: completed

Delivered:

- JSON article schema
- resource loader
- repository cache
- graceful handling for malformed or missing content

### Phase 5: Search

Status: completed for MVP baseline

Delivered:

- query normalization
- alias-aware search
- immediate in-memory result lookup

Follow-up work:

- improve alias coverage
- tune ranking as the article set grows

### Phase 6: Article Rendering and Navigation

Status: completed for MVP baseline

Delivered:

- article sections rendered from loaded data
- related topic navigation
- version note display
- article scrolling

### Phase 7: First-Open UX

Status: completed for MVP baseline

Delivered:

- home state when search is empty
- default topic list before filtering
- explicit unavailable state when the whole wiki fails to load

Follow-up work:

- bookmarks
- featured/common topics

### Phase 8: Starter Content Expansion

Status: next major step

Deliverables:

- first meaningful curated article batch
- alias coverage for common phrasings
- consistency review for brevity and style

Acceptance criteria:

- enough topics exist that the home/result list feels useful
- core queries like `villager`, `mending`, `diamond y level`, and `slime` return useful results

### Phase 9: Search Quality Pass

Status: pending

Deliverables:

- improved ranking heuristics
- expanded aliases
- regression checks for obvious player phrasing

Acceptance criteria:

- common survival queries reliably open the intended article first
- additional content does not noticeably degrade search responsiveness

## Agent-Friendly Work Split

This is the intended parallelization boundary for future work.

### Workstream A: Screen and UX

Owns:

- `PauseMenuIntegration`
- `SmallWikiScreen`
- layout and interaction polish
- bookmarks and home-state UX

Should not own:

- article writing
- search ranking tuning

### Workstream B: Content System and Search

Owns:

- `ContentPackLoader`
- `ArticleRepository`
- `SearchService`
- search ranking behavior
- content load failure semantics

Should not own:

- pause menu layout changes
- large article-writing batches

### Workstream C: Starter Content

Owns:

- article JSON files
- aliases
- consistency review

Should not own:

- renderer logic
- search implementation details

## Resolved Decisions

- Minecraft target: `1.21.10`
- Base package: `com.ingamewiki`
- Mod ID: `ingamewiki`
- Java target: `21`
- Article loading for MVP: eager load at client startup
- Lazy content loading: deferred unless performance justifies it later
- Pause-menu entrypoint: Fabric screen events, not mixins

## Risks

### Scope Creep

Risk:

- trying to become a full wiki too early

Mitigation:

- enforce one-screen articles and a fixed starter topic set

### Version Drift

Risk:

- vanilla facts change across versions

Mitigation:

- target one version first and label version-sensitive content explicitly

### UI Complexity

Risk:

- overbuilding the first screen before data and search are proven useful

Mitigation:

- keep the current vanilla-widget foundation and add UX layers incrementally

### Content Maintenance

Risk:

- adding content faster than it can be reviewed for accuracy

Mitigation:

- keep article structure fixed and review additions in small batches
