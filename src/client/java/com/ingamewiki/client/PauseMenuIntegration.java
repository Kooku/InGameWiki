package com.ingamewiki.client;

import com.ingamewiki.client.ui.SmallWikiScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

public final class PauseMenuIntegration {
	private PauseMenuIntegration() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof PauseScreen)) {
				return;
			}

			int buttonWidth = 204;
			int buttonHeight = 20;
			int buttonX = (scaledWidth - buttonWidth) / 2;
			int buttonY = Screens.getButtons(screen).stream()
				.map(AbstractWidget.class::cast)
				.mapToInt(widget -> widget.getY() + widget.getHeight())
				.max()
				.orElse(scaledHeight / 4 + 120) + 4;

			Screens.getButtons(screen).add(
				Button.builder(Component.translatable("menu.ingamewiki.open"), button -> client.setScreen(new SmallWikiScreen(screen)))
					.bounds(buttonX, buttonY, buttonWidth, buttonHeight)
					.build()
			);
		});
	}
}
