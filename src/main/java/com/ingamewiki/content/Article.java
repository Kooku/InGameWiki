package com.ingamewiki.content;

import java.util.List;

public record Article(
	String id,
	String title,
	List<String> aliases,
	String category,
	String versionNote,
	String quickAnswer,
	List<String> keyFacts,
	List<String> commonMistakes,
	List<String> relatedTopics
) {
	public Article {
		aliases = aliases == null ? List.of() : List.copyOf(aliases);
		keyFacts = keyFacts == null ? List.of() : List.copyOf(keyFacts);
		commonMistakes = commonMistakes == null ? List.of() : List.copyOf(commonMistakes);
		relatedTopics = relatedTopics == null ? List.of() : List.copyOf(relatedTopics);
	}

	public static Article unavailable(String articleId, String title, String quickAnswer, List<String> keyFacts) {
		return new Article(
			articleId,
			title,
			List.of(),
			"system",
			"",
			quickAnswer,
			keyFacts,
			List.of(),
			List.of()
		);
	}
}
