package com.ingamewiki.content;

import java.util.List;

public record ContentPack(
	String packId,
	String displayName,
	String subtitle,
	String targetName,
	String searchPlaceholder,
	String homeTitle,
	String homeHint,
	String homeSidebarHint,
	String noSelectionHint,
	List<String> homeExamples
) {
	public static final ContentPack FALLBACK = new ContentPack(
		"missing-pack",
		"InGameWiki",
		"",
		"",
		"Search topics...",
		"Welcome to InGameWiki",
		"The current content pack failed to load.",
		"Start typing to search available topics.",
		"Try a different search or review the game log for pack errors.",
		List.of("villager restocking", "diamond y level", "librarian mending")
	);

	public ContentPack {
		homeExamples = homeExamples == null ? List.of() : List.copyOf(homeExamples);
	}
}
