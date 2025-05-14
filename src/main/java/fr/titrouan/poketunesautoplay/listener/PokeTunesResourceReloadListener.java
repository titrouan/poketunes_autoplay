package fr.titrouan.poketunesautoplay.listener;

import fr.titrouan.poketunesautoplay.PokeTunesAutoPlay;
import fr.titrouan.poketunesautoplay.PokeTunesMusic;
import fr.titrouan.poketunesautoplay.PokeTunesSoundLoader;
import fr.titrouan.poketunesautoplay.config.LangHelper;
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
        System.out.println(LangHelper.get("log.resreload.trigger.load.soundsjson"));
        List<PokeTunesMusic> musics = PokeTunesSoundLoader.loadGameMusics();

        if (musics.isEmpty()) {
            System.err.println(LangHelper.get("log.resreload.error.musics.load.soundsjson"));
        } else {
            musicManager.setAvailableMusics(musics);
            musicManager.markSoundsLoaded();
            System.out.println(LangHelper.get("log.resreload.musics.load.soundsjson"));

            PokeTunesAutoPlay.RESOURCES_READY = true;
            System.out.println(LangHelper.get("log.resreload.resources.ready"));
            System.out.println(LangHelper.get("log.resreload.resources.reloaded", musics.size()));
        }
    }
}