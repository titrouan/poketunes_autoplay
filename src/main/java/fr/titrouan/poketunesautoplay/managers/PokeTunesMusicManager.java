package fr.titrouan.poketunesautoplay.managers;

import fr.titrouan.poketunesautoplay.PokeTunesAutoPlay;
import fr.titrouan.poketunesautoplay.access.SoundManagerMixinBridge;
import fr.titrouan.poketunesautoplay.config.ConfigManager;
import fr.titrouan.poketunesautoplay.fade.MusicFadesManager;
import fr.titrouan.poketunesautoplay.PokeTunesMusic;
import fr.titrouan.poketunesautoplay.sound.CustomPositionedSoundInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class PokeTunesMusicManager {

    private final MinecraftClient client;
    private static final Random random = new Random();
    private final MusicFadesManager fadesManager;
    private boolean firstLaunch = true ;
    private boolean menuFirstLaunch = true ;

    // Musiques dynamiquement chargées depuis sounds.json
    //Musics dynamically loaded from sounds.json
    private List<PokeTunesMusic> availableMusics = null;
    private boolean soundsLoaded = false ;

    private int ticksUntilNextSong = getRandomDelay();
    private SoundInstance currentMusic = null ;
    private boolean menuReady = false;

    public PokeTunesMusicManager(MinecraftClient client) {
        this.client = client;
        this.fadesManager = new MusicFadesManager(client);
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
        if (MinecraftClient.getInstance() == null || client.isPaused()) return;
        // Autoriser si on est dans un monde OU dans le menu principal
        // Grant access if you are in a world OR in the main menu
        if (client.world == null && client.currentScreen == null) return;
        if (!soundsLoaded) return;

        //boolean isInGame = client.world != null;
        boolean isInMenu = /*client.world == null && */client.currentScreen instanceof TitleScreen;

        /**
         * Si une musique est en cours, vérifier si elle s’est terminée
         * If a music is playing, check if it is finished
         */
        if (currentMusic != null && !client.getSoundManager().isPlaying(currentMusic)) {
            System.out.println("[PokeTunes AutoPlay] La musique s’est terminée, réinitialisation du statut.");
            ((SoundManagerMixinBridge) client.getSoundManager()).poketunesautoplay$resetMusicStatus();
            currentMusic = null;
        }

        //System.out.println("[DEBUG] isMusicPlaying = " + isMusicPlaying());
        if (!isMusicPlaying()) {
            if (isInMenu && menuFirstLaunch) {
                if (!menuReady) {
                    //Ne pas lancer la musique/Do not play music
                    return;
                }
                ticksUntilNextSong = 40;
                menuFirstLaunch = false ;
            } else if (!isInMenu && firstLaunch) {
                // else if pour + de gestion, mais sinon else suffit.
                ticksUntilNextSong = 200;
                firstLaunch = false;
            }
            if (ticksUntilNextSong <= 0) {
                playRandomSong();
                ticksUntilNextSong = getRandomDelay();
            } else {
                ticksUntilNextSong--;
                //System.out.println("[PokeTunes AutoPlay] Ticks restants : " + ticksUntilNextSong);
                //System.out.println("[PokeTunes AutoPlay] Remaining ticks : " + ticksUntilNextSong);
            }
        }
    }

    /**
     * Joue une musique aléatoire avec son volume défini et fondu d'entrée.
     * Plays a random music with its defined volume and fade-in effect.
     */
    private void playRandomSong() {
        System.out.println("[PokeTunes AutoPlay] Tentative de lecture d'une musique...");
        System.out.println("[PokeTunes AutoPlay] Attempt to play music...");

        if (!PokeTunesAutoPlay.RESOURCES_READY) {
            System.err.println("[PokeTunes AutoPlay] Les ressources ne sont pas encore prêtes, annulation de la lecture.");
            return;
        }

        String currentCategory = getCurrentCategoryFromWorld();
        List<PokeTunesMusic> filtered = availableMusics.stream()
                .filter(m -> m.getCategory().equals(currentCategory))
                .toList();
        if (filtered.isEmpty()) {
            System.out.println("[PokeTunes AutoPlay] Aucune musique trouvée pour la catégorie : " + currentCategory);
            System.out.println("[PokeTunes AutoPlay] No music found for this category : " + currentCategory);
            return;
        }

        PokeTunesMusic selected = filtered.get(random.nextInt(filtered.size()));
        Identifier id = selected.id;
        float volume = selected.volume;

        System.out.println("[PokeTunes AutoPlay] Lecture de : " + id + " (volume max : " + volume + ")");
        System.out.println("[PokeTunes AutoPlay] Playback of : " + id + " (max volume : " + volume + ")");

        SoundEvent soundEvent = Registries.SOUND_EVENT.get(id);
        if (soundEvent == null) {
            System.err.println("[PokeTunes AutoPlay] SoundEvent introuvable : " + id);
            System.err.println("[PokeTunes AutoPlay] SoundEvent not found : " + id);
            return;
        } else {
            System.out.println("[PokeTunes AutoPlay] SoundEvent récupéré : " + soundEvent.getId());
            System.out.println("[PokeTunes AutoPlay] Retrieved SoundEvent : " + soundEvent.getId());
        }
        currentMusic = new CustomPositionedSoundInstance(id, selected.sourcePath, 1.0f);

        fadesManager.startFadeIn(currentMusic, ConfigManager.fadeIn, volume);
    }

    /**
     * Vérifie si une musique est actuellement jouée.
     * Checks if a music is currently playing.
     */
    private boolean isMusicPlaying() {
        return ((SoundManagerMixinBridge) client.getSoundManager()).poketunesautoplay$isMusicPlayingBridge();
    }

    public void setAvailableMusics(List<PokeTunesMusic> musics) {
        this.availableMusics = musics;
    }

    public void markSoundsLoaded() {
        this.soundsLoaded = true ;
    }

    /**
     * Définit la catégorie en fonction de la dimension ou du menu.
     * The dimension where you are (or menu) defines the category to return.
     * @return String
     */
    private String getCurrentCategoryFromWorld() {
        if (client.world == null || client.player == null) return "menu";
        if (client.world.getRegistryKey() == World.NETHER) return "nether";
        if (client.world.getRegistryKey() == World.END) return "end";
        // Pour la dimension Safari / For the Safari dimension
        if (client.world.getRegistryKey().getValue().equals(new Identifier("academy", "safari"))) return "safari";

        //overworld par défaut / Overworld by default
        return "game";
    }

    public List<PokeTunesMusic> getAvailableMusics() {
        return availableMusics;
    }

    public void markMenuReady() {
        this.menuReady = true;
        this.menuFirstLaunch = true;
    }
}