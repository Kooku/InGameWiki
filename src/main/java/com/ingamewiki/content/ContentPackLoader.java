package com.ingamewiki.content;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ingamewiki.InGameWikiMod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ContentPackLoader {
	private static final Gson GSON = new Gson();
	private static final String DEFAULT_PACK_PATH = "data/ingamewiki/pack.json";

	private ContentPackLoader() {
	}

	public static LoadResult loadBundledPack() {
		PackManifest manifest;
		try {
			manifest = readResource(DEFAULT_PACK_PATH, PackManifest.class);
		} catch (IllegalStateException exception) {
			InGameWikiMod.LOGGER.error("InGameWiki content pack manifest failed to load", exception);
			return new LoadResult(
				ContentPack.FALLBACK,
				List.of(),
				Map.of(),
				"InGameWiki couldn't load its content pack manifest. Other gameplay should still work, but the wiki is temporarily unavailable."
			);
		}

		ContentPack contentPack = toContentPack(manifest);

		ArticleIndex index;
		try {
			index = readResource(manifest.articlesIndexPath(), ArticleIndex.class);
		} catch (IllegalStateException exception) {
			InGameWikiMod.LOGGER.error("InGameWiki article index failed to load for pack {}", manifest.packId(), exception);
			return new LoadResult(
				contentPack,
				List.of(),
				Map.of(),
				"InGameWiki couldn't load the article index for the active content pack. Other gameplay should still work, but the wiki is temporarily unavailable."
			);
		}

		List<Article> articles = new ArrayList<>();
		Map<String, String> failures = new LinkedHashMap<>();
		String articleRoot = articleRoot(manifest.articlesIndexPath());

		for (String articleId : index.articles()) {
			String articlePath = articleRoot + articleId + ".json";
			try {
				Article article = readResource(articlePath, Article.class);
				articles.add(article);
			} catch (IllegalStateException exception) {
				InGameWikiMod.LOGGER.error("InGameWiki article {} failed to load for pack {}", articleId, manifest.packId(), exception);
				failures.put(
					articleId,
					"Something went wrong while loading this page. Other InGameWiki pages should still work while the pack author takes a look."
				);
			}
		}

		return new LoadResult(contentPack, List.copyOf(articles), Map.copyOf(failures), null);
	}

	private static ContentPack toContentPack(PackManifest manifest) {
		PackHome home = manifest.home();
		return new ContentPack(
			valueOrDefault(manifest.packId(), "default"),
			valueOrDefault(manifest.displayName(), "InGameWiki"),
			Objects.requireNonNullElse(manifest.subtitle(), ""),
			Objects.requireNonNullElse(manifest.targetName(), ""),
			valueOrDefault(home.searchPlaceholder(), "Search topics..."),
			valueOrDefault(home.title(), "Welcome to InGameWiki"),
			valueOrDefault(home.hint(), "Search for a topic to get a fast in-game answer."),
			valueOrDefault(home.sidebarHint(), "Start typing to search available topics."),
			valueOrDefault(home.noSelectionHint(), "Try a different search term."),
			home.examples()
		);
	}

	private static String articleRoot(String articlesIndexPath) {
		int lastSlash = articlesIndexPath.lastIndexOf('/');
		if (lastSlash < 0) {
			return "";
		}
		return articlesIndexPath.substring(0, lastSlash + 1);
	}

	private static String valueOrDefault(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value;
	}

	private static <T> T readResource(String path, Class<T> type) {
		try (InputStream stream = ContentPackLoader.class.getClassLoader().getResourceAsStream(path)) {
			if (stream == null) {
				throw new IllegalStateException("Missing bundled resource: " + path);
			}

			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				T parsed = GSON.fromJson(reader, type);
				if (parsed == null) {
					throw new IllegalStateException("Parsed null resource: " + path);
				}
				return parsed;
			}
		} catch (IOException | JsonParseException exception) {
			InGameWikiMod.LOGGER.error("Failed to load bundled resource {}", path, exception);
			throw new IllegalStateException("Failed to load bundled resource: " + path, exception);
		}
	}

	private record PackManifest(
		String packId,
		String displayName,
		String subtitle,
		String targetName,
		String articlesIndexPath,
		PackHome home
	) {
		private PackManifest {
			home = home == null ? PackHome.EMPTY : home;
		}
	}

	private record PackHome(
		String title,
		String hint,
		String sidebarHint,
		String noSelectionHint,
		String searchPlaceholder,
		List<String> examples
	) {
		private static final PackHome EMPTY = new PackHome("", "", "", "", "", List.of());

		private PackHome {
			examples = examples == null ? List.of() : List.copyOf(examples);
		}
	}

	private record ArticleIndex(List<String> articles) {
		private ArticleIndex {
			articles = articles == null ? List.of() : List.copyOf(articles);
		}
	}

	public record LoadResult(
		ContentPack contentPack,
		List<Article> articles,
		Map<String, String> failures,
		String globalFailureMessage
	) {
		public LoadResult {
			contentPack = contentPack == null ? ContentPack.FALLBACK : contentPack;
			articles = articles == null ? List.of() : List.copyOf(articles);
			failures = failures == null ? Map.of() : Map.copyOf(failures);
		}

		public boolean hasGlobalFailure() {
			return globalFailureMessage != null && !globalFailureMessage.isBlank();
		}
	}
}
