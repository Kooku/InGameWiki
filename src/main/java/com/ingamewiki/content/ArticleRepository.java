package com.ingamewiki.content;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ArticleRepository {
	private final Map<String, Article> articlesById = new LinkedHashMap<>();

	public void loadBundled() {
		articlesById.clear();
		for (Article article : ArticleLoader.loadBundledArticles()) {
			articlesById.put(article.id(), article);
		}
	}

	public Collection<Article> all() {
		return articlesById.values().stream()
			.sorted(Comparator.comparing(Article::title, String.CASE_INSENSITIVE_ORDER))
			.toList();
	}

	public Optional<Article> findById(String articleId) {
		return Optional.ofNullable(articlesById.get(articleId));
	}
}
