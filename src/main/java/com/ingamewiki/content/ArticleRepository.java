package com.ingamewiki.content;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ArticleRepository {
	private final Map<String, Article> articlesById = new LinkedHashMap<>();
	private final Map<String, String> failedArticleMessages = new LinkedHashMap<>();
	private ContentPack contentPack = ContentPack.FALLBACK;
	private String globalFailureMessage;

	public void loadBundled() {
		articlesById.clear();
		failedArticleMessages.clear();
		contentPack = ContentPack.FALLBACK;
		globalFailureMessage = null;

		ContentPackLoader.LoadResult loadResult = ContentPackLoader.loadBundledPack();
		contentPack = loadResult.contentPack();
		globalFailureMessage = loadResult.globalFailureMessage();
		failedArticleMessages.putAll(loadResult.failures());

		for (Article article : loadResult.articles()) {
			articlesById.put(article.id(), article);
		}
	}

	public Collection<Article> all() {
		return articlesById.values().stream()
			.sorted(Comparator.comparing(Article::title, String.CASE_INSENSITIVE_ORDER))
			.toList();
	}

	public ContentPack contentPack() {
		return contentPack;
	}

	public Optional<Article> findById(String articleId) {
		return Optional.ofNullable(articlesById.get(articleId));
	}

	public Optional<Article> fallbackForId(String articleId) {
		if (articleId == null || articleId.isBlank()) {
			return Optional.empty();
		}

		if (failedArticleMessages.containsKey(articleId)) {
			return Optional.of(Article.unavailable(
				articleId,
				"Article unavailable",
				failedArticleMessages.get(articleId),
				List.of("Article ID: " + articleId)
			));
		}

		if (globalFailureMessage != null && !globalFailureMessage.isBlank()) {
			return globalFailureArticle();
		}

		return Optional.of(Article.unavailable(
			articleId,
			"Page not found",
			"This page is missing right now. Other InGameWiki pages may still be available.",
			List.of("Article ID: " + articleId)
		));
	}

	public Optional<Article> globalFailureArticle() {
		if (globalFailureMessage == null || globalFailureMessage.isBlank()) {
			return Optional.empty();
		}

		return Optional.of(Article.unavailable(
			"ingamewiki-unavailable",
			"InGameWiki unavailable",
			globalFailureMessage,
			List.of("The developer has been notified through the game logs.")
		));
	}
}
