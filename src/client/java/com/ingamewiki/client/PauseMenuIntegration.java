package com.ingamewiki.client;

import com.ingamewiki.client.ui.SmallWikiScreen;
import com.ingamewiki.content.ContentPack;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

public final class PauseMenuIntegration {
	private static final int BUTTON_SPACING = 24;
	private static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
	private static final Component DISCONNECT = Component.translatable("menu.disconnect");

	private PauseMenuIntegration() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof PauseScreen)) {
				return;
			}

			ContentPack contentPack = InGameWikiClient.articleRepository().contentPack();
			List<AbstractWidget> existingButtons = Screens.getButtons(screen);
			int buttonWidth = 204;
			int buttonX = (scaledWidth - buttonWidth) / 2;
			AbstractWidget saveAndQuitButton = existingButtons.stream()
				.filter(PauseMenuIntegration::isExitButton)
				.findFirst()
				.orElseGet(() -> existingButtons.stream()
					.filter(widget -> widget.getWidth() >= buttonWidth)
					.max(Comparator.comparingInt(AbstractWidget::getY))
					.orElse(null));

			int buttonY = scaledHeight / 4 + 144;
			if (saveAndQuitButton != null) {
				buttonY = saveAndQuitButton.getY();
				((LayoutElement) saveAndQuitButton).setY(saveAndQuitButton.getY() + BUTTON_SPACING);
			}

			existingButtons.add(
				Button.builder(Component.literal(contentPack.displayName()), button -> client.setScreen(new SmallWikiScreen(screen)))
					.bounds(buttonX, buttonY, buttonWidth, 20)
					.build()
			);
		});
	}

	private static boolean isExitButton(AbstractWidget widget) {
		if (widget.getWidth() < 204) {
			return false;
		}

		String message = widget.getMessage().getString();
		return message.equals(RETURN_TO_MENU.getString()) || message.equals(DISCONNECT.getString());
	}
}
