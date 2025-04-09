package fr.titrouan.poketunesautoplay.listener;

import fr.titrouan.poketunesautoplay.PokeTunesAutoPlay;
import fr.titrouan.poketunesautoplay.PokeTunesMusic;
import fr.titrouan.poketunesautoplay.PokeTunesSoundLoader;
import fr.titrouan.poketunesautoplay.managers.PokeTunesMusicManager;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.List;

public class PokeTunesResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    private final PokeTunesMusicManager musicManager;

    public PokeTunesResourceReloadListener(PokeTunesMusicManager musicManager) {
        this.musicManager = musicManager;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(PokeTunesAutoPlay.MOD_ID, "sounds_json_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        System.out.println("[PokeTunes AutoPlay] Déclenchement du ResourceReloadListener : rechargement de sounds.json");
        System.out.println("[PokeTunes AutoPlay] ResourceReloadListener trigger: sounds.json reload");
        List<PokeTunesMusic> musics = PokeTunesSoundLoader.loadGameMusics();

        if (musics.isEmpty()) {
            System.err.println("[PokeTunes AutoPlay] Échec du chargement de musiques depuis sounds.json !");
            System.err.println("[PokeTunes AutoPlay] Failed to load musics from sounds.json !");
        } else {
            musicManager.setAvailableMusics(musics);
            musicManager.markSoundsLoaded();
            System.out.println("[PokeTunes AutoPlay] Musiques rechargées avec succès !");
            System.out.println("[PokeTunes AutoPlay] Musics successfully reloaded !");

            PokeTunesAutoPlay.RESOURCES_READY = true;
            System.out.println("[PokeTunes AutoPlay] Les ressources sont prêtes !");
            System.out.println("[PokeTunes AutoPlay] Resources are ready !");
            System.out.println("[PokeTunes AutoPlay] Musiques rechargées avec succès ! Total : " + musics.size());
            System.out.println("[PokeTunes AutoPlay] Musics successfully reloaded ! Total : " + musics.size());
        }
    }
}