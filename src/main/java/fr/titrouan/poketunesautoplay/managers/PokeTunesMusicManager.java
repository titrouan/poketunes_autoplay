package fr.titrouan.poketunesautoplay.managers;

import fr.titrouan.poketunesautoplay.access.SoundManagerMixinBridge;
import fr.titrouan.poketunesautoplay.config.ConfigManager;
import fr.titrouan.poketunesautoplay.fade.MusicFadesManager;
import fr.titrouan.poketunesautoplay.PokeTunesMusic;
import fr.titrouan.poketunesautoplay.PokeTunesSoundLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Random;

public class PokeTunesMusicManager {

    private final MinecraftClient client;
    private static final Random random = new Random();
    private final MusicFadesManager fadesManager;

    // Musiques dynamiques chargées depuis sounds.json
    private List<PokeTunesMusic> availableMusics = null;

    private int ticksUntilNextSong = getRandomDelay();
    private SoundInstance currentMusic = null ;

    public PokeTunesMusicManager(MinecraftClient client) {
        this.client = client;
        this.fadesManager = new MusicFadesManager(client);
        //this.availableMusics = PokeTunesSoundLoader.loadGameMusics();
    }

    /**
     * Calcule un délai aléatoire entre 2 musiques, selon la configuration.
     * Calculates a random delay between 2 songs, based on the configuration.
     */
    private static int getRandomDelay() {
        int min = ConfigManager.minDelay;
        int max = ConfigManager.maxDelay;
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Appelé à chaque tick client.
     * Called every client tick.
     */
    public void tick() {
        if (client.world == null || client.isPaused()) return;

        /**
         * Si une musique est en cours, vérifier si elle s’est terminée
         * If a music is playing, check if it is finished
         */
        if (currentMusic != null && !client.getSoundManager().isPlaying(currentMusic)) {
            System.out.println("[PokeTunes AutoPlay] La musique s’est terminée, réinitialisation du statut.");
            ((SoundManagerMixinBridge) client.getSoundManager()).poketunesautoplay$resetMusicStatus();
            currentMusic = null;
        }

        System.out.println("[DEBUG] isMusicPlaying = " + isMusicPlaying());
        if (!isMusicPlaying()) {
            if (ticksUntilNextSong <= 0) {
                playRandomSong();
                ticksUntilNextSong = getRandomDelay();
            } else {
                ticksUntilNextSong--;
                System.out.println("[PokeTunes AutoPlay] Ticks restants : " + ticksUntilNextSong);
            }
        }
    }

    /**
     * Joue une musique aléatoire avec son volume défini et fondu d'entrée.
     * Plays a random music with its defined volume and fade-in effect.
     */
    private void playRandomSong() {
        System.out.println("[PokeTunes AutoPlay] Tentative de lecture d'une musique...");
        if (availableMusics == null) {
            availableMusics = PokeTunesSoundLoader.loadGameMusics();
        }

        if (availableMusics.isEmpty()) {
            System.err.println("[PokeTunes AutoPlay] Aucune musique disponible dans music.game !");
            return;
        }

        PokeTunesMusic selected = availableMusics.get(random.nextInt(availableMusics.size()));
        Identifier id = selected.id;
        float volume = selected.volume;

        System.out.println("[PokeTunes AutoPlay] Lecture de : " + id + " (volume max : " + volume + ")");

        SoundEvent soundEvent = SoundEvent.of(id);
        currentMusic = PositionedSoundInstance.music(soundEvent);

        fadesManager.startFadeIn(currentMusic, ConfigManager.fadeIn, volume);
    }

    /**
     * Vérifie si une musique est actuellement jouée.
     * Checks if a music is currently playing.
     */
    private boolean isMusicPlaying() {
        return ((SoundManagerMixinBridge) client.getSoundManager()).poketunesautoplay$isMusicPlayingBridge();
    }
}