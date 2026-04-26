package com.ingamewiki.client.ui;

import com.ingamewiki.client.InGameWikiClient;
import com.ingamewiki.content.Article;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SmallWikiScreen extends Screen {
	private static final int RESULT_BUTTON_COUNT = 6;
	private static final int OUTER_PADDING = 12;
	private static final int INNER_PADDING = 12;
	private static final int PANEL_GAP = 12;
	private static final int PANEL_BACKGROUND = 0xAA101010;
	private static final int PANEL_BORDER = 0x80FFFFFF;
	private static final int SELECTED_RESULT_BACKGROUND = 0x80446A9F;
	private static final int ARTICLE_SCROLL_STEP = 18;
	private static final int LINK_HEIGHT = 12;

	private final Screen parent;
	private final List<Button> resultButtons = new ArrayList<>();
	private final List<RelatedTopicHitbox> relatedTopicHitboxes = new ArrayList<>();
	private List<Article> currentResults = List.of();

	private EditBox searchField;
	private Article selectedArticle;
	private int articleScroll;
	private int articleContentHeight;

	public SmallWikiScreen(Screen parent) {
		super(Component.translatable("screen.ingamewiki.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		Layout layout = layout();

		searchField = new EditBox(this.font, layout.leftPanelX + INNER_PADDING, layout.panelTop + INNER_PADDING, layout.leftInnerWidth, 20, Component.translatable("screen.ingamewiki.search"));
		searchField.setMaxLength(100);
		searchField.setHint(Component.translatable("screen.ingamewiki.search_placeholder"));
		searchField.setResponder(query -> refreshSearch(query, true));
		addRenderableWidget(searchField);
		setInitialFocus(searchField);

		addRenderableWidget(
			Button.builder(Component.translatable("gui.back"), button -> onClose())
				.bounds(layout.rightPanelRight - 60 - INNER_PADDING, layout.panelTop + INNER_PADDING, 60, 20)
				.build()
		);

		for (int index = 0; index < RESULT_BUTTON_COUNT; index++) {
			final int resultIndex = index;
			Button resultButton = Button.builder(Component.empty(), button -> selectResult(resultIndex))
				.bounds(layout.leftPanelX + INNER_PADDING, layout.resultsTop + (index * 24), layout.leftInnerWidth, 20)
				.build();
			resultButtons.add(addRenderableWidget(resultButton));
		}

		refreshSearch(searchField.getValue(), true);
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(parent);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (verticalAmount == 0) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		Layout layout = layout();
		if (layout.isInArticleArea(mouseX, mouseY)) {
			articleScroll = clampArticleScroll(articleScroll - (int) Math.round(verticalAmount * ARTICLE_SCROLL_STEP), selectedArticle);
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			for (RelatedTopicHitbox hitbox : relatedTopicHitboxes) {
				if (hitbox.contains(event.x(), event.y())) {
					openArticleById(hitbox.articleId());
					return true;
				}
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int keyCode = event.key();

		if (keyCode == 264) {
			moveSelection(1);
			return true;
		}

		if (keyCode == 265) {
			moveSelection(-1);
			return true;
		}

		if ((keyCode == 257 || keyCode == 335) && !currentResults.isEmpty()) {
			int selectedIndex = indexOfSelectedArticle();
			if (selectedIndex < 0) {
				selectedIndex = 0;
			}
			selectedArticle = currentResults.get(selectedIndex);
			articleScroll = 0;
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		renderMenuBackground(guiGraphics);

		Layout layout = layout();
		relatedTopicHitboxes.clear();

		guiGraphics.fill(layout.leftPanelX, layout.panelTop, layout.leftPanelRight, layout.panelBottom, PANEL_BACKGROUND);
		guiGraphics.fill(layout.rightPanelX, layout.panelTop, layout.rightPanelRight, layout.panelBottom, PANEL_BACKGROUND);
		guiGraphics.hLine(layout.leftPanelX, layout.leftPanelRight - 1, layout.panelTop, PANEL_BORDER);
		guiGraphics.hLine(layout.rightPanelX, layout.rightPanelRight - 1, layout.panelTop, PANEL_BORDER);
		guiGraphics.hLine(layout.leftPanelX, layout.leftPanelRight - 1, layout.panelBottom - 1, PANEL_BORDER);
		guiGraphics.hLine(layout.rightPanelX, layout.rightPanelRight - 1, layout.panelBottom - 1, PANEL_BORDER);
		guiGraphics.vLine(layout.leftPanelX, layout.panelTop, layout.panelBottom - 1, PANEL_BORDER);
		guiGraphics.vLine(layout.leftPanelRight - 1, layout.panelTop, layout.panelBottom - 1, PANEL_BORDER);
		guiGraphics.vLine(layout.rightPanelX, layout.panelTop, layout.panelBottom - 1, PANEL_BORDER);
		guiGraphics.vLine(layout.rightPanelRight - 1, layout.panelTop, layout.panelBottom - 1, PANEL_BORDER);

		super.render(guiGraphics, mouseX, mouseY, delta);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, layout.panelTop + 4, 0xFFFFFF);
		guiGraphics.drawString(this.font, Component.translatable("screen.ingamewiki.results"), layout.leftPanelX + INNER_PADDING, layout.resultsLabelY, 0xC8C8C8);

		drawSelectedResultHighlight(guiGraphics);
		drawLeftPanelMessages(guiGraphics, layout);

		Article articleToRender = selectedArticle;
		if (articleToRender == null) {
			articleToRender = InGameWikiClient.articleRepository().globalFailureArticle().orElse(null);
		}

		if (articleToRender == null) {
			drawRightPanelEmptyState(guiGraphics, layout);
			return;
		}

		int articleTextX = layout.rightPanelX + INNER_PADDING;
		int articleTextWidth = layout.rightInnerWidth;
		int articleViewportTop = layout.articleViewportTop;
		int articleViewportBottom = layout.articleViewportBottom;
		int articleViewportHeight = articleViewportBottom - articleViewportTop;

		articleContentHeight = measureArticleHeight(articleToRender, articleTextWidth);
		articleScroll = clampArticleScroll(articleScroll, articleToRender);

		guiGraphics.enableScissor(layout.rightPanelX + 1, articleViewportTop, layout.rightPanelRight - 1, articleViewportBottom);
		renderArticle(guiGraphics, articleToRender, articleTextX, articleViewportTop - articleScroll, articleTextWidth, mouseX, mouseY);
		guiGraphics.disableScissor();

		drawScrollHint(guiGraphics, layout, articleViewportHeight);
	}

	private void drawLeftPanelMessages(GuiGraphics guiGraphics, Layout layout) {
		String query = searchField == null ? "" : searchField.getValue().trim();
		if (!query.isEmpty() && currentResults.isEmpty()) {
			guiGraphics.drawWordWrap(
				this.font,
				Component.translatable("screen.ingamewiki.no_results", query),
				layout.leftPanelX + INNER_PADDING,
				layout.resultsTop + 4,
				layout.leftInnerWidth,
				0xD8D8D8
			);
		}
	}

	private void drawRightPanelEmptyState(GuiGraphics guiGraphics, Layout layout) {
		int textX = layout.rightPanelX + INNER_PADDING;
		guiGraphics.drawString(this.font, Component.translatable("screen.ingamewiki.no_selection"), textX, layout.articleViewportTop, 0xFFFFFF);
		guiGraphics.drawWordWrap(
			this.font,
			Component.translatable("screen.ingamewiki.no_selection_hint"),
			textX,
			layout.articleViewportTop + 16,
			layout.rightInnerWidth,
			0xA0A0A0
		);
	}

	private void drawSelectedResultHighlight(GuiGraphics guiGraphics) {
		if (selectedArticle == null) {
			return;
		}

		for (int index = 0; index < currentResults.size() && index < resultButtons.size(); index++) {
			Article article = currentResults.get(index);
			if (!article.id().equals(selectedArticle.id())) {
				continue;
			}

			Button button = resultButtons.get(index);
			guiGraphics.fill(button.getX() + 1, button.getY() + 1, button.getX() + button.getWidth() - 1, button.getY() + button.getHeight() - 1, SELECTED_RESULT_BACKGROUND);
			break;
		}
	}

	private void drawScrollHint(GuiGraphics guiGraphics, Layout layout, int articleViewportHeight) {
		if (articleContentHeight <= articleViewportHeight) {
			return;
		}

		String scrollLabel = (articleScroll > 0 ? "\u2191 " : "") + (articleScroll < maxArticleScroll() ? "\u2193" : "");
		guiGraphics.drawString(this.font, Component.literal(scrollLabel.trim()), layout.rightPanelRight - INNER_PADDING - 10, layout.articleViewportTop, 0xA0A0A0);
	}

	private void renderArticle(GuiGraphics guiGraphics, Article article, int x, int y, int width, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.literal(article.title()), x, y, 0xFFFFFF);
		y += 14;

		if (!article.versionNote().isBlank()) {
			guiGraphics.drawString(this.font, Component.literal(article.versionNote()), x, y, 0xA0A0A0);
			y += 18;
		}

		y = drawSection(guiGraphics, Component.translatable("screen.ingamewiki.section.quick_answer"), Component.literal(article.quickAnswer()), x, y, width, 0xFFFFFF);
		y = drawBulletedSection(guiGraphics, Component.translatable("screen.ingamewiki.section.key_facts"), article.keyFacts(), x, y, width, 0xFFFFFF);
		y = drawBulletedSection(guiGraphics, Component.translatable("screen.ingamewiki.section.common_mistakes"), article.commonMistakes(), x, y, width, 0xFFFFFF);

		if (!article.relatedTopics().isEmpty()) {
			guiGraphics.drawString(this.font, Component.translatable("screen.ingamewiki.section.related_topics"), x, y, 0xFFE082);
			y += 12;
			for (String relatedId : article.relatedTopics()) {
				Optional<Article> relatedArticle = InGameWikiClient.articleRepository()
					.findById(relatedId)
					.or(() -> InGameWikiClient.articleRepository().fallbackForId(relatedId));
				String relatedTitle = relatedArticle.map(Article::title).orElse(relatedId);
				boolean hovered = isLinkHovered(mouseX, mouseY, x, y, width);
				int color = hovered ? 0xFFFFFF : 0x7DC9FF;
				guiGraphics.drawString(this.font, Component.literal("> " + relatedTitle), x, y, color);
				relatedTopicHitboxes.add(new RelatedTopicHitbox(relatedId, x, y, width, LINK_HEIGHT));
				y += LINK_HEIGHT + 2;
			}
		}
	}

	private int measureArticleHeight(Article article, int width) {
		int y = 0;
		y += 14;

		if (!article.versionNote().isBlank()) {
			y += 18;
		}

		y = measureSection(Component.literal(article.quickAnswer()), y, width);
		y = measureBulletedSection(article.keyFacts(), y, width);
		y = measureBulletedSection(article.commonMistakes(), y, width);

		if (!article.relatedTopics().isEmpty()) {
			y += 12;
			y += article.relatedTopics().size() * (LINK_HEIGHT + 2);
		}

		return y;
	}

	private int drawSection(GuiGraphics guiGraphics, Component heading, Component body, int x, int y, int width, int bodyColor) {
		guiGraphics.drawString(this.font, heading, x, y, 0xFFE082);
		y += 12;
		return drawWrappedText(guiGraphics, body, x, y, width, bodyColor) + 4;
	}

	private int drawBulletedSection(GuiGraphics guiGraphics, Component heading, List<String> items, int x, int y, int width, int bodyColor) {
		if (items.isEmpty()) {
			return y;
		}

		guiGraphics.drawString(this.font, heading, x, y, 0xFFE082);
		y += 12;
		for (String item : items) {
			y = drawWrappedText(guiGraphics, Component.literal("• " + item), x, y, width, bodyColor);
		}
		return y + 4;
	}

	private int measureSection(Component body, int y, int width) {
		y += 9;
		y += 12;
		y += this.font.wordWrapHeight(body, width);
		return y + 4;
	}

	private int measureBulletedSection(List<String> items, int y, int width) {
		if (items.isEmpty()) {
			return y;
		}

		y += 9;
		y += 12;
		for (String item : items) {
			y += this.font.wordWrapHeight(Component.literal("• " + item), width);
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
			articleScroll = 0;
		} else if (selectedArticle != null && currentResults.stream().noneMatch(article -> article.id().equals(selectedArticle.id()))) {
			selectedArticle = currentResults.isEmpty() ? null : currentResults.getFirst();
			articleScroll = 0;
		}
	}

	private void selectResult(int resultIndex) {
		if (resultIndex >= 0 && resultIndex < currentResults.size()) {
			selectedArticle = currentResults.get(resultIndex);
			articleScroll = 0;
		}
	}

	private void moveSelection(int delta) {
		if (currentResults.isEmpty()) {
			return;
		}

		int currentIndex = selectedArticle == null ? 0 : indexOfSelectedArticle();
		if (currentIndex < 0) {
			currentIndex = 0;
		}

		int nextIndex = Math.max(0, Math.min(currentResults.size() - 1, currentIndex + delta));
		selectedArticle = currentResults.get(nextIndex);
		articleScroll = 0;
	}

	private int indexOfSelectedArticle() {
		if (selectedArticle == null) {
			return -1;
		}

		for (int index = 0; index < currentResults.size(); index++) {
			if (currentResults.get(index).id().equals(selectedArticle.id())) {
				return index;
			}
		}

		return -1;
	}

	private void openArticleById(String articleId) {
		selectedArticle = InGameWikiClient.articleRepository()
			.findById(articleId)
			.or(() -> InGameWikiClient.articleRepository().fallbackForId(articleId))
			.orElse(selectedArticle);
		articleScroll = 0;
	}

	private int clampArticleScroll(int proposedScroll, Article article) {
		if (article == null) {
			return 0;
		}

		int maxScroll = maxArticleScroll();
		if (proposedScroll < 0) {
			return 0;
		}

		return Math.min(proposedScroll, maxScroll);
	}

	private int maxArticleScroll() {
		Layout layout = layout();
		int viewportHeight = layout.articleViewportBottom - layout.articleViewportTop;
		return Math.max(0, articleContentHeight - viewportHeight);
	}

	private boolean isLinkHovered(int mouseX, int mouseY, int x, int y, int width) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + LINK_HEIGHT;
	}

	private Layout layout() {
		int panelTop = OUTER_PADDING;
		int panelBottom = this.height - OUTER_PADDING;
		int panelHeight = panelBottom - panelTop;
		int leftPanelWidth = Math.max(190, Math.min(220, this.width / 4));
		int leftPanelX = OUTER_PADDING;
		int rightPanelX = leftPanelX + leftPanelWidth + PANEL_GAP;
		int rightPanelWidth = this.width - rightPanelX - OUTER_PADDING;
		int leftInnerWidth = leftPanelWidth - (INNER_PADDING * 2);
		int rightInnerWidth = rightPanelWidth - (INNER_PADDING * 2);
		int resultsLabelY = panelTop + 52;
		int resultsTop = resultsLabelY + 14;
		int articleViewportTop = panelTop + 32;
		int articleViewportBottom = panelBottom - INNER_PADDING;

		return new Layout(
			panelTop,
			panelBottom,
			panelHeight,
			leftPanelX,
			leftPanelX + leftPanelWidth,
			leftPanelWidth,
			leftInnerWidth,
			rightPanelX,
			rightPanelX + rightPanelWidth,
			rightInnerWidth,
			resultsLabelY,
			resultsTop,
			articleViewportTop,
			articleViewportBottom
		);
	}

	private record Layout(
		int panelTop,
		int panelBottom,
		int panelHeight,
		int leftPanelX,
		int leftPanelRight,
		int leftPanelWidth,
		int leftInnerWidth,
		int rightPanelX,
		int rightPanelRight,
		int rightInnerWidth,
		int resultsLabelY,
		int resultsTop,
		int articleViewportTop,
		int articleViewportBottom
	) {
		private boolean isInArticleArea(double mouseX, double mouseY) {
			return mouseX >= rightPanelX && mouseX <= rightPanelRight && mouseY >= articleViewportTop && mouseY <= articleViewportBottom;
		}
	}

	private record RelatedTopicHitbox(String articleId, int x, int y, int width, int height) {
		private boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}
	}
}
