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
import java.util.List;

public final class ArticleLoader {
	private static final Gson GSON = new Gson();
	private static final String ARTICLES_ROOT = "data/ingamewiki/articles/";
	private static final String ARTICLE_INDEX_PATH = ARTICLES_ROOT + "index.json";

	private ArticleLoader() {
	}

	public static List<Article> loadBundledArticles() {
		ArticleIndex index = readResource(ARTICLE_INDEX_PATH, ArticleIndex.class);
		List<Article> articles = new ArrayList<>();

		for (String articleId : index.articles()) {
			String articlePath = ARTICLES_ROOT + articleId + ".json";
			articles.add(readResource(articlePath, Article.class));
		}

		return List.copyOf(articles);
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
}
