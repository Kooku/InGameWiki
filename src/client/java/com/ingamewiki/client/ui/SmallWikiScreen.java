package com.ingamewiki.client.ui;

import com.ingamewiki.client.InGameWikiClient;
import com.ingamewiki.content.Article;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SmallWikiScreen extends Screen {
	private static final int RESULT_BUTTON_COUNT = 6;

	private final Screen parent;
	private final List<Button> resultButtons = new ArrayList<>();
	private List<Article> currentResults = List.of();

	private EditBox searchField;
	private Article selectedArticle;

	public SmallWikiScreen(Screen parent) {
		super(Component.translatable("screen.ingamewiki.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int leftPanelX = 16;
		int leftPanelWidth = 170;

		searchField = new EditBox(this.font, leftPanelX, 24, leftPanelWidth, 20, Component.translatable("screen.ingamewiki.search"));
		searchField.setMaxLength(100);
		searchField.setHint(Component.translatable("screen.ingamewiki.search_placeholder"));
		searchField.setResponder(query -> refreshSearch(query, true));
		addRenderableWidget(searchField);
		setInitialFocus(searchField);

		addRenderableWidget(
			Button.builder(Component.translatable("gui.back"), button -> onClose())
				.bounds(this.width - 76, 12, 60, 20)
				.build()
		);

		for (int index = 0; index < RESULT_BUTTON_COUNT; index++) {
			final int resultIndex = index;
			Button resultButton = Button.builder(Component.empty(), button -> selectResult(resultIndex))
				.bounds(leftPanelX, 52 + (index * 24), leftPanelWidth, 20)
				.build();
			resultButtons.add(addRenderableWidget(resultButton));
		}

		refreshSearch("", true);
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(parent);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		renderBackground(guiGraphics, mouseX, mouseY, delta);

		int leftPanelX = 12;
		int leftPanelWidth = 178;
		int rightPanelX = 206;
		int rightPanelWidth = this.width - rightPanelX - 16;
		int panelTop = 12;
		int panelBottom = this.height - 12;

		guiGraphics.fill(leftPanelX, panelTop, leftPanelX + leftPanelWidth, panelBottom, 0xAA101010);
		guiGraphics.fill(rightPanelX, panelTop, rightPanelX + rightPanelWidth, panelBottom, 0xAA101010);

		super.render(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);

		guiGraphics.drawString(this.font, Component.translatable("screen.ingamewiki.results"), 20, 58, 0xC8C8C8);

		Article articleToRender = selectedArticle;
		if (articleToRender == null) {
			articleToRender = InGameWikiClient.articleRepository().globalFailureArticle().orElse(null);
		}

		if (articleToRender == null) {
			guiGraphics.drawString(this.font, Component.translatable("screen.ingamewiki.no_selection"), rightPanelX + 12, 28, 0xFFFFFF);
			guiGraphics.drawString(this.font, Component.translatable("screen.ingamewiki.no_selection_hint"), rightPanelX + 12, 44, 0xA0A0A0);
			return;
		}

		int textX = rightPanelX + 12;
		int textY = 28;
		int textWidth = rightPanelWidth - 24;

		guiGraphics.drawString(this.font, Component.literal(articleToRender.title()), textX, textY, 0xFFFFFF);
		textY += 14;

		if (!articleToRender.versionNote().isBlank()) {
			guiGraphics.drawString(this.font, Component.literal(articleToRender.versionNote()), textX, textY, 0xA0A0A0);
			textY += 18;
		}

		textY = drawSection(guiGraphics, Component.translatable("screen.ingamewiki.section.quick_answer"), Component.literal(articleToRender.quickAnswer()), textX, textY, textWidth);
		textY = drawBulletedSection(guiGraphics, Component.translatable("screen.ingamewiki.section.key_facts"), articleToRender.keyFacts(), textX, textY, textWidth);
		textY = drawBulletedSection(guiGraphics, Component.translatable("screen.ingamewiki.section.common_mistakes"), articleToRender.commonMistakes(), textX, textY, textWidth);

		if (!articleToRender.relatedTopics().isEmpty()) {
			guiGraphics.drawString(this.font, Component.translatable("screen.ingamewiki.section.related_topics"), textX, textY, 0xFFE082);
			textY += 12;
			for (String relatedId : articleToRender.relatedTopics()) {
				String relatedTitle = InGameWikiClient.articleRepository()
					.findById(relatedId)
					.or(() -> InGameWikiClient.articleRepository().fallbackForId(relatedId))
					.map(Article::title)
					.orElse(relatedId);
				textY = drawWrappedText(guiGraphics, Component.literal("• " + relatedTitle), textX, textY, textWidth, 0xC8D6FF);
			}
		}
	}

	private int drawSection(GuiGraphics guiGraphics, Component heading, Component body, int x, int y, int width) {
		guiGraphics.drawString(this.font, heading, x, y, 0xFFE082);
		y += 12;
		return drawWrappedText(guiGraphics, body, x, y, width, 0xFFFFFF) + 4;
	}

	private int drawBulletedSection(GuiGraphics guiGraphics, Component heading, List<String> items, int x, int y, int width) {
		if (items.isEmpty()) {
			return y;
		}

		guiGraphics.drawString(this.font, heading, x, y, 0xFFE082);
		y += 12;
		for (String item : items) {
			y = drawWrappedText(guiGraphics, Component.literal("• " + item), x, y, width, 0xFFFFFF);
		}
		return y + 4;
	}

	private int drawWrappedText(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color) {
		guiGraphics.drawWordWrap(this.font, text, x, y, width, color);
		return y + this.font.wordWrapHeight(text, width);
	}

	private void refreshSearch(String query, boolean autoSelectTopResult) {
		currentResults = InGameWikiClient.searchService().search(query, RESULT_BUTTON_COUNT);

		for (int index = 0; index < resultButtons.size(); index++) {
			Button button = resultButtons.get(index);
			if (index < currentResults.size()) {
				Article article = currentResults.get(index);
				button.visible = true;
				button.active = true;
				button.setMessage(Component.literal(article.title()));
			} else {
				button.visible = false;
				button.active = false;
				button.setMessage(Component.empty());
			}
		}

		if (autoSelectTopResult) {
			selectedArticle = currentResults.isEmpty() ? null : currentResults.getFirst();
		}
	}

	private void selectResult(int resultIndex) {
		if (resultIndex >= 0 && resultIndex < currentResults.size()) {
			selectedArticle = currentResults.get(resultIndex);
		}
	}
}
