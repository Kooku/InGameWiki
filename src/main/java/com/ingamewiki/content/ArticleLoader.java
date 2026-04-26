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

public final class ArticleLoader {
	private static final Gson GSON = new Gson();
	private static final String ARTICLES_ROOT = "data/ingamewiki/articles/";
	private static final String ARTICLE_INDEX_PATH = ARTICLES_ROOT + "index.json";

	private ArticleLoader() {
	}

	public static LoadResult loadBundledArticles() {
		ArticleIndex index;
		try {
			index = readResource(ARTICLE_INDEX_PATH, ArticleIndex.class);
		} catch (IllegalStateException exception) {
			InGameWikiMod.LOGGER.error("InGameWiki article index failed to load", exception);
			return new LoadResult(
				List.of(),
				Map.of(),
				"InGameWiki couldn't load its article index. Other gameplay should still work, but the wiki is temporarily unavailable."
			);
		}

		List<Article> articles = new ArrayList<>();
		Map<String, String> failures = new LinkedHashMap<>();

		for (String articleId : index.articles()) {
			String articlePath = ARTICLES_ROOT + articleId + ".json";
			try {
				Article article = readResource(articlePath, Article.class);
				articles.add(article);
			} catch (IllegalStateException exception) {
				InGameWikiMod.LOGGER.error("InGameWiki article {} failed to load", articleId, exception);
				failures.put(
					articleId,
					"Something went wrong while loading this page. Other InGameWiki pages should still work while the developer takes a look."
				);
			}
		}

		return new LoadResult(List.copyOf(articles), Map.copyOf(failures), null);
	}

	private static <T> T readResource(String path, Class<T> type) {
		try (InputStream stream = ArticleLoader.class.getClassLoader().getResourceAsStream(path)) {
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

	private record ArticleIndex(List<String> articles) {
		private ArticleIndex {
			articles = articles == null ? List.of() : List.copyOf(articles);
		}
	}

	public record LoadResult(
		List<Article> articles,
		Map<String, String> failures,
		String globalFailureMessage
	) {
		public LoadResult {
			articles = articles == null ? List.of() : List.copyOf(articles);
			failures = failures == null ? Map.of() : Map.copyOf(failures);
		}

		public boolean hasGlobalFailure() {
			return globalFailureMessage != null && !globalFailureMessage.isBlank();
		}
	}
}
