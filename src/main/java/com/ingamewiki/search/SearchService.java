package com.ingamewiki.search;

import com.ingamewiki.content.Article;
import com.ingamewiki.content.ArticleRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class SearchService {
	private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

	private final ArticleRepository repository;

	public SearchService(ArticleRepository repository) {
		this.repository = repository;
	}

	public List<Article> search(String query, int limit) {
		String normalizedQuery = normalize(query);
		if (normalizedQuery.isBlank()) {
			return repository.all().stream().limit(limit).toList();
		}

		List<String> queryTokens = tokenize(normalizedQuery);
		List<ScoredArticle> matches = new ArrayList<>();

		for (Article article : repository.all()) {
			int score = score(article, normalizedQuery, queryTokens);
			if (score > 0) {
				matches.add(new ScoredArticle(article, score));
			}
		}

		return matches.stream()
			.sorted(Comparator
				.comparingInt(ScoredArticle::score)
				.reversed()
				.thenComparing(result -> result.article().title(), String.CASE_INSENSITIVE_ORDER))
			.limit(limit)
			.map(ScoredArticle::article)
			.toList();
	}

	private int score(Article article, String normalizedQuery, List<String> queryTokens) {
		int score = 0;
		String normalizedTitle = normalize(article.title());

		if (normalizedTitle.equals(normalizedQuery)) {
			score += 1000;
		} else if (normalizedTitle.contains(normalizedQuery)) {
			score += 300;
		}

		for (String alias : article.aliases()) {
			String normalizedAlias = normalize(alias);
			if (normalizedAlias.equals(normalizedQuery)) {
				score += 900;
			} else if (normalizedAlias.contains(normalizedQuery)) {
				score += 240;
			}
		}

		score += tokenScore(normalizedTitle, queryTokens, 60);
		for (String alias : article.aliases()) {
			score += tokenScore(normalize(alias), queryTokens, 45);
		}

		score += tokenScore(normalize(article.quickAnswer()), queryTokens, 12);
		return score;
	}

	private int tokenScore(String haystack, List<String> tokens, int tokenValue) {
		int score = 0;
		for (String token : tokens) {
			if (!token.isBlank() && haystack.contains(token)) {
				score += tokenValue;
			}
		}
		return score;
	}

	private String normalize(String value) {
		return NON_ALPHANUMERIC.matcher(Objects.requireNonNullElse(value, "").toLowerCase(Locale.ROOT))
			.replaceAll(" ")
			.trim();
	}

	private List<String> tokenize(String normalizedValue) {
		if (normalizedValue.isBlank()) {
			return List.of();
		}
		return List.of(normalizedValue.split(" "));
	}

	private record ScoredArticle(Article article, int score) {
	}
}
