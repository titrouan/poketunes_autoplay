package fr.titrouan.poketunesautoplay;

import fr.titrouan.poketunesautoplay.listener.PokeTunesResourceReloadListener;
import fr.titrouan.poketunesautoplay.managers.PokeTunesMusicManager;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.titrouan.poketunesautoplay.config.ConfigManager;

import java.util.List;

public class PokeTunesAutoPlay implements ClientModInitializer {
	public static final String MOD_ID = "poketunesautoplay";

	/**
	 * Ce logger est utilisé pour écrire du texte dans la console et le fichier log.
	 * Il est recommandé d’utiliser votre mod ID comme nom pour le logger.
	 * De cette façon, il sera facile de savoir quel mod aura généré des infos, des warnings et des erreurs.
	 *
	 * This logger is used to write text to the console and the log file.
	 * It is considered best practice to use your mod id as the logger's name.
	 * That way, it's clear which mod wrote info, warnings, and errors.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean RESOURCES_READY = false;

	public static final PokeTunesMusicManager musicManager = new PokeTunesMusicManager(MinecraftClient.getInstance());

	@Override
	public void onInitializeClient() {
		LOGGER.info("Hello Fabric world, it's PokeTunes AutoPlay !");
		LOGGER.info("[PokeTunes AutoPlay] Initialisation du mod / Mod initialization");

		// Étape 1 : Chargement de la configuration
		// Step 1 : Loading the configuration
		ConfigManager.loadConfig();
		LOGGER.info("[PokeTunes AutoPlay] Configuration chargée / Configuration loaded");

		// Étape 2 : Enregistrement du listener de reload pour charger les musiques du pack
		//Step 2 : Recording the reload listener to load the pack’s music
		net.fabricmc.fabric.api.resource.ResourceManagerHelper
				.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(new PokeTunesResourceReloadListener(musicManager));

		// Étape 3 : Gestion des ticks client
		// Step 3 : Managing client ticks
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Stopper le MusicTracker vanilla à chaque tick (juste au cas où il tente de relancer une musique)
			// Stop the vanilla MusicTracker at each tick (just in case it tries to re-play a song)
			client.getMusicTracker().stop();
			musicManager.tick();
		});

		// Étape 4 : Enregistre manuellement tous les SoundEvent extraits du sounds.json
		// Step 4 : Manually record all extracted SoundEvent from the sounds.json
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			List<PokeTunesMusic> musics = PokeTunesSoundLoader.loadGameMusics();
			for (PokeTunesMusic music : musics) {
				Identifier soundId = music.id;
				if (!Registries.SOUND_EVENT.containsId(soundId)) {
					Registry.register(Registries.SOUND_EVENT, soundId, SoundEvent.of(soundId));
                    //LOGGER.info("[PokeTunes AutoPlay] SoundEvent enregistré à l'ouverture du client : {}", soundId);
					//LOGGER.info("[PokeTunes AutoPlay] SoundEvent recorded at client opening : {}", soundId);
				}
			}
			// Mets à jour les musiques du manager aussi, comme dans le reload()
			// Update the manager’s music too, as in reload()
			PokeTunesAutoPlay.musicManager.setAvailableMusics(musics);
			PokeTunesAutoPlay.musicManager.markSoundsLoaded();
			PokeTunesAutoPlay.RESOURCES_READY = true;
		});
	}
}