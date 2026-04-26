package com.ingamewiki;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InGameWikiMod implements ModInitializer {
	public static final String MOD_ID = "ingamewiki";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing InGameWiki");
	}
}
