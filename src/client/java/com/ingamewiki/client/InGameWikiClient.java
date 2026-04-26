package com.ingamewiki.client;

import com.ingamewiki.content.ArticleRepository;
import com.ingamewiki.search.SearchService;
import net.fabricmc.api.ClientModInitializer;

public final class InGameWikiClient implements ClientModInitializer {
	private static final ArticleRepository ARTICLE_REPOSITORY = new ArticleRepository();
	private static final SearchService SEARCH_SERVICE = new SearchService(ARTICLE_REPOSITORY);

	@Override
	public void onInitializeClient() {
		ARTICLE_REPOSITORY.loadBundled();
		PauseMenuIntegration.register();
	}

	public static ArticleRepository articleRepository() {
		return ARTICLE_REPOSITORY;
	}

	public static SearchService searchService() {
		return SEARCH_SERVICE;
	}
}
