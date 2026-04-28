# Contributing

`InGameWiki` is structured as a small wiki engine plus a bundled content pack. The current repo ships the vanilla pack, but the code should stay neutral enough that a fork can swap in `Create`, `Immersive Engineering`, or another single-mod pack without rewriting the UI.

## Design Rule

Pack-specific knowledge belongs in JSON resources, not in Java code.

That means:

- articles should be data
- home-screen copy should be data
- example searches should be data
- pack identity and article roots should be data
- search tuning should prefer data-backed fields before custom logic

Java should own the reusable engine:

- screen layout
- pause-menu integration
- loading and validation
- search mechanics
- navigation and failure states

## Current Layout

```text
src/main/java/com/ingamewiki/content/
  ContentPack.java
  ContentPackLoader.java
  Article.java
  ArticleRepository.java

src/main/resources/data/ingamewiki/
  pack.json
  packs/vanilla/
    articles/
      index.json
      *.json
```

`pack.json` selects the active bundled pack and defines pack-owned UI text. The article index then points to the pack's article files.

## Creating a New Pack

For a new mod-specific fork:

1. Keep the shared engine code unless the mod truly needs new behavior.
2. Replace `src/main/resources/data/ingamewiki/pack.json` with your pack metadata.
3. Add your article files under a new pack root such as `data/ingamewiki/packs/create/articles/`.
4. Point `articlesIndexPath` at that new index file.
5. Update the icon, mod description, and branding strings if the fork should ship under a new name.

A minimal `pack.json` looks like this:

```json
{
  "packId": "create",
  "displayName": "InGameWiki - Create",
  "subtitle": "Create Machine Guide",
  "targetName": "Create",
  "articlesIndexPath": "data/ingamewiki/packs/create/articles/index.json",
  "home": {
    "title": "Welcome to InGameWiki - Create",
    "hint": "Search for a Create machine or process to get a fast in-game answer.",
    "sidebarHint": "Start typing to search topics like cogwheel, stress, depot, or sequenced assembly.",
    "noSelectionHint": "Try a common Create term in the search box.",
    "searchPlaceholder": "Search stress, depot, crafter...",
    "examples": [
      "mechanical crafter",
      "stress units",
      "sequenced assembly"
    ]
  }
}
```

## Article Contract

Each article is a small JSON document with this shape:

```json
{
  "id": "mechanical-crafter",
  "title": "Mechanical Crafter",
  "aliases": ["crafter", "create crafter", "auto crafting"],
  "keywords": ["crafting chain", "connected crafters", "automation"],
  "category": "automation",
  "versionNote": "Create 0.x",
  "quickAnswer": "Use a connected line or grid of Mechanical Crafters for shaped automated crafting recipes.",
  "keyFacts": [
    "Each crafter contributes one slot to the recipe layout.",
    "Belt or depot delivery is often easier than direct insertion while prototyping."
  ],
  "commonMistakes": [
    "Assuming a single crafter can perform multi-slot shaped recipes."
  ],
  "relatedTopics": ["depot", "sequenced-assembly"]
}
```

Field intent:

- `aliases` cover likely player phrasing
- `keywords` cover ranking hints that do not read naturally as titles or aliases
- `relatedTopics` should stay inside the same pack unless you are intentionally shipping cross-pack references

## Forking Guidance

A fork should usually change data first, not architecture.

Good reasons to change Java:

- the mod needs a different article section model
- the search UI needs a genuinely different interaction
- the pack needs runtime integration that cannot be expressed as static content

Bad reasons to change Java:

- different home-screen examples
- different categories
- different search vocabulary
- different article coverage

## Content Quality

Keep pack articles:

- short
- practical
- version-aware
- consistent in tone
- scoped to one target domain at a time

If a fork starts accumulating unrelated mods in one pack, the search and home-state UX will get muddy quickly. Prefer one pack per mod or one pack for vanilla.
