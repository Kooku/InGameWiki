# Implementation Plan

## Goal

Build the first playable version of `InGameWiki` as a Fabric client-side mod for vanilla Minecraft players on Realms and servers.

The first version should let a player:

1. open `smallWiki` from the pause menu
2. search a topic in plain language
3. read a concise in-game answer
4. follow related topics without leaving the game

## Scope Boundary

This plan is for the first playable prototype and MVP.

Included:

- Fabric project setup
- pause-menu entry point
- `smallWiki` screen
- local article data
- manual search
- article rendering
- starter content set

Excluded:

- contextual triggers
- hover hints
- browser handoff
- remote data fetching
- server-side features
- modded content coverage

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

- Use Java 21
- Lock the exact toolchain version during scaffolding

### Data Strategy

- Ship curated article data inside the mod resources
- Use JSON for article definitions
- Build a lightweight in-memory search index at client startup for the MVP
- Revisit lazy loading only if startup or content scale becomes a real problem

### UI Strategy

- Use vanilla screen widgets
- Add one pause-menu button to open `smallWiki`
- Start with a two-pane layout:
  - left: search box and results
  - right: selected article

## Proposed Project Structure

This structure is intentionally simple and separates UI, content, and search.

```text
src/main/java/.../
  InGameWikiMod.java
  client/
    InGameWikiClient.java
    ui/
      SmallWikiScreen.java
      PauseMenuMixin.java
      widget/
    content/
      Article.java
      ArticleSection.java
      ArticleRepository.java
      ArticleLoader.java
    search/
      SearchIndex.java
      SearchQuery.java
      SearchResult.java
      SearchService.java

src/main/resources/
  fabric.mod.json
  ingamewiki.mixins.json
  assets/ingamewiki/lang/en_us.json
  data/ingamewiki/articles/*.json
```

Use `com.ingamewiki` as the base package when scaffolding. The separation should remain.

## Content Model

Each article should be a small, strongly typed unit rather than raw markdown.

Suggested fields:

- `id`
- `title`
- `aliases`
- `category`
- `version_range` or `version_note`
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

## Search Plan

V1 search should be simple and deterministic.

### Ranking Inputs

- exact title match
- exact alias match
- token overlap with title
- token overlap with aliases
- optional keyword overlap with article body fields

### Search Rules

- normalize to lowercase
- ignore punctuation
- singular/plural tolerance where possible
- return top result selected by default
- keep ranking explainable and easy to tune

### Non-Goals For Search

- no fuzzy ML ranking
- no embeddings
- no external search library unless the vanilla implementation proves insufficient

## UI Plan

### Entry Point

- Add a `smallWiki` button to the pause screen
- Open the main screen without closing the current world/session

### Main Screen

The initial layout should support fast lookup, not browsing depth.

Required elements:

- search text box
- result list
- article title
- version label if applicable
- `Quick Answer`
- `Key Facts`
- `Common Mistakes`
- `Related Topics`

Optional later polish:

- category chips
- recent searches
- keyboard shortcuts

### Empty States

Define these before implementation so the UX does not stall:

- no query entered
- no results found
- content load failure
- article selected but missing related topic

## Phased Delivery

### Phase 1: Fabric Foundation

Deliverables:

- Gradle and Fabric project setup
- client entrypoint
- mod metadata
- run configuration works locally

Acceptance criteria:

- the mod launches in a development client
- the mod is visible in the loaded mod list

### Phase 2: Pause Menu Integration

Deliverables:

- pause menu button
- screen open/close wiring

Acceptance criteria:

- player can open and close `smallWiki` from an active world
- pause menu layout is not broken at common resolutions

### Phase 3: Screen Skeleton

Deliverables:

- `SmallWikiScreen`
- search input
- placeholder result list
- placeholder article panel

Acceptance criteria:

- the screen renders reliably
- keyboard and mouse navigation work
- placeholder data can be selected and displayed

### Phase 4: Content Loading

Deliverables:

- JSON article schema
- resource loader
- repository cache
- error handling for malformed or missing content

Acceptance criteria:

- bundled article files load correctly
- invalid files fail gracefully and are logged clearly

### Phase 5: Search

Deliverables:

- query normalization
- alias-aware ranking
- sorted search results

Acceptance criteria:

- obvious queries resolve to the expected article
- empty queries do not crash or stall the screen
- search feels immediate with the starter dataset

### Phase 6: Article Rendering

Deliverables:

- article sections rendered from loaded data
- related topic navigation
- version note display

Acceptance criteria:

- a player can read the full article without overlapping UI
- related topics navigate correctly

### Phase 7: Starter Content Pass

Deliverables:

- first curated article batch
- alias coverage for common phrasings
- review for brevity and consistency

Acceptance criteria:

- at least 20 high-value articles exist
- core queries like `villager`, `mending`, `diamond y level`, and `slime` return useful results

## Agent-Friendly Work Split

This is the intended parallelization boundary once implementation starts.

### Workstream A: Fabric and UI Shell

Owns:

- Gradle setup
- Fabric bootstrap
- mixins
- pause menu button
- `SmallWikiScreen`

Should not own:

- article writing
- search ranking tuning

### Workstream B: Content System and Search

Owns:

- article schema
- JSON loading
- repository/cache
- search index and ranking

Should not own:

- pause menu integration
- widget layout styling

### Workstream C: Starter Content

Owns:

- article JSON files
- aliases
- consistency review

Should not own:

- renderer logic
- low-level search implementation

## Resolved Decisions

- Minecraft target: `1.21.10`
- Base package: `com.ingamewiki`
- Mod ID: `ingamewiki`
- Article loading for MVP: eager load at client startup
- Lazy content loading: deferred unless performance justifies it later
- Java target: `21`
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

- use vanilla widgets first and defer custom visual polish

### Content Maintenance

Risk:

- adding content faster than it can be reviewed for accuracy

Mitigation:

- keep the initial set small and high-value

## Definition of Done for MVP

The MVP is done when all of the following are true:

- A player can open `smallWiki` from the pause menu in a Fabric client.
- The mod ships with a local article set.
- Search works for common plain-language queries.
- Articles render entirely in-game with no browser handoff.
- The initial content set covers the most common vanilla survival lookups.
- The experience is stable in normal play on a Realm or server because the mod remains fully client-side.

## Supporting Spec

See [docs/build-spec.md](/Users/kuyoungshin/Coding/InGameWiki/docs/build-spec.md) for the locked setup choices that should guide scaffolding.
