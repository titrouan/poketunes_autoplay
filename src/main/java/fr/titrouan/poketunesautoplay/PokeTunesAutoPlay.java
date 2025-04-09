package fr.titrouan.poketunesautoplay;

import fr.titrouan.poketunesautoplay.managers.PokeTunesMusicManager;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.titrouan.poketunesautoplay.config.ConfigManager;

public class PokeTunesAutoPlay implements ClientModInitializer {
	public static final String MOD_ID = "poketunesautoplay";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final PokeTunesMusicManager musicManager = new PokeTunesMusicManager(MinecraftClient.getInstance());

	@Override
	public void onInitializeClient() {
		LOGGER.info("Hello Fabric world, it's PokeTunes AutoPlay !");

		LOGGER.info("[PokeTunes AutoPlay] Initialisation du mod / Mod initialization");
		ConfigManager.loadConfig();
		LOGGER.info("[PokeTunes AutoPlay] Configuration chargée / Configuration loaded");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			PokeTunesAutoPlay.musicManager.tick();
		});
	}
}