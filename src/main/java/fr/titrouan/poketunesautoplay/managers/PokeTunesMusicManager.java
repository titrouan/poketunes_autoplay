package fr.titrouan.poketunesautoplay.managers;

import fr.titrouan.poketunesautoplay.PokeTunesAutoPlay;
import fr.titrouan.poketunesautoplay.access.OptionsScreenMixinBridge;
import fr.titrouan.poketunesautoplay.audio.PokeTunesAudioPlayer;
import fr.titrouan.poketunesautoplay.config.ConfigManager;
import fr.titrouan.poketunesautoplay.config.LangHelper;
import fr.titrouan.poketunesautoplay.PokeTunesMusic;
import fr.titrouan.poketunesautoplay.sound.SoundSuppressor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;

public class PokeTunesMusicManager {

    private final MinecraftClient client;
    private static final Random random = new Random();
    private boolean justChangedContext = false;
    private boolean mustPlayFirstTrack = false;
    private boolean firstLaunch = true ;
    private boolean menuFirstLaunch = true ;
    private boolean wasInGame = false;
    private Screen lastScreen = null ;

    // Musiques dynamiquement chargées depuis sounds.json
    //Musics dynamically loaded from sounds.json
    private List<PokeTunesMusic> availableMusics = null;
    private boolean soundsLoaded = false ;

    private int ticksUntilNextSong = -1;
    private int initialDelay = -1;
    private PokeTunesMusic currentMusic = null ;
    private boolean menuReady = false;
    private final PokeTunesAudioPlayer player = new PokeTunesAudioPlayer();
    private boolean shouldAutoPauseNextTrack = false;
    private boolean forceNextTrack = false;
    private final Map<String, LinkedList<PokeTunesMusic>> playHistory = new HashMap<>();
    private final Map<String, Integer> maxRecentTracks = Map.of(
            "menu", 3,
            "safari", 3,
            "nether", 3,
            "game", 8,
            "end", 5
    );

    private int parasiteBlockTicks = -1;

    public PokeTunesMusicManager(MinecraftClient client) {
        this.client = client;
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

    public PokeTunesAudioPlayer getPlayer() {
        return this.player;
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
        boolean isInMenu = client.currentScreen instanceof TitleScreen;

        player.tick();

        if (client.currentScreen instanceof OptionsScreen optionsScreen) {
            OptionsScreenMixinBridge bridge = (OptionsScreenMixinBridge) optionsScreen;
            bridge.poketunesautoplay$updateCurrentTrack(getCurrentMusic());
            bridge.poketunesautoplay$tickScrollingText();

            bridge.poketunesautoplay$updatePlayPauseIcon(player.isPaused());
        }

        /**
         * Si une musique est en cours, vérifier si elle s’est terminée
         * If a music is playing, check if it is finished
         */
        if (currentMusic != null && !player.isPlaying()) {
            System.out.println(LangHelper.get("log.musicmanager.music.finished"));
            currentMusic = null;
        }

        //System.out.println(LangHelper.get("log.musicmanager.ismusicplaying", player.isPlaying()));
        if (!player.isPlaying() || forceNextTrack) {
                if (!menuReady && isInMenu) {
                    //Ne pas lancer la musique/Do not play music
                    return;
                }
            if (justChangedContext || menuFirstLaunch) {
                if (ticksUntilNextSong <= 0) {
                    String category = getCurrentCategoryFromWorld();
                    if (category.equals("menu") || category.equals("end")) {
                        mustPlayFirstTrack = true;
                    } else {
                        mustPlayFirstTrack = false;
                    }
                    if (mustPlayFirstTrack) {
                        playFirstTrackForCategory(category);
                        if (isInMenu) menuFirstLaunch = false;
                    } else {
                        playRandomSong();
                    }
                    ticksUntilNextSong = getRandomDelay();
                    justChangedContext = false;
                } else {
                    ticksUntilNextSong--;
                }
                return;
            } else {

                // Comportement classique (hors contexte spécial)
                // Normal behavior (out of special context)
                if (ticksUntilNextSong <= 0) {
                    playRandomSong();
                    ticksUntilNextSong = getRandomDelay();
                } else {
                    ticksUntilNextSong--;
                }
            }
        }

        if (parasiteBlockTicks > 0) {
            parasiteBlockTicks--;
            if (parasiteBlockTicks == 0) {
                SoundSuppressor.deactivate();
                parasiteBlockTicks = -1;
            }
        }
    }

    /**
     * Joue la première musique disponible dans la catégorie spécifiée (ordre de la liste).
     * Used to force playback of the first track of a category (e.g., on entering menu or End).
     */
    private void playFirstTrackForCategory(String category) {
        System.out.println(LangHelper.get("log.musicmanager.firstmusic.get.category", category));

        if (!PokeTunesAutoPlay.RESOURCES_READY) {
            System.err.println(LangHelper.get("log.musicmanager.error.resources.notready"));
            return;
        }

        LinkedList<PokeTunesMusic> history = playHistory.getOrDefault(category, new LinkedList<>());
        List<PokeTunesMusic> allTracks = availableMusics.stream()
                .filter(m -> m.getCategory().equals(category))
                .toList();
        List<PokeTunesMusic> candidates = allTracks.stream()
                .filter(track -> !history.contains(track))
                .toList();
        if (allTracks.isEmpty() || candidates.isEmpty()) {
            System.out.println(LangHelper.get("log.musicmanager.nomusic.get.category", category));
            playHistory.remove(category);
            return;
        }

        PokeTunesMusic selected = candidates.get(0);

        //System.out.println(LangHelper.get("log.musicmanager.music.playing", selected.id, selected.volume));
        //System.out.println("[PokeTunes AutoPlay] Lecture de : " + selected.id + " (volume max : " + selected.volume + ")");
        //System.out.println("[PokeTunes AutoPlay] Playback of : " + selected.id + " (max volume : " + selected.volume + ")");
        currentMusic = selected;

        player.play(currentMusic, ConfigManager.fadeIn);
        if (shouldAutoPauseNextTrack) {
            PokeTunesAutoPlay.togglePaused(); // on remet la musique en pause / we pause the music again
            shouldAutoPauseNextTrack = false;
            forceNextTrack = false;
        }
        addToHistory(category, currentMusic);
    }

    /**
     * Joue une musique aléatoire avec son volume défini et fondu d'entrée.
     * Plays a random music with its defined volume and fade-in effect.
     */
    private void playRandomSong() {
        System.out.println(LangHelper.get("log.musicmanager.music.launch"));

        if (!PokeTunesAutoPlay.RESOURCES_READY) {
            System.err.println(LangHelper.get("log.musicmanager.error.resources.notready"));
            return;
        }

        String currentCategory = getCurrentCategoryFromWorld();
        LinkedList<PokeTunesMusic> history = playHistory.getOrDefault(currentCategory, new LinkedList<>());
        List<PokeTunesMusic> allTracks = availableMusics.stream()
                .filter(m -> m.getCategory().equals(currentCategory))
                .toList();
        List<PokeTunesMusic> candidates = allTracks.stream()
                .filter(track -> !history.contains(track))
                .toList();
        if (allTracks.isEmpty() || candidates.isEmpty()) {
            System.out.println(LangHelper.get("log.musicmanager.nomusic.get.category" ,currentCategory));
            playHistory.remove(currentCategory);
            return;
        }

        PokeTunesMusic selected = candidates.get(random.nextInt(candidates.size()));
        Identifier id = selected.id;
        float volume = selected.volume;

        //System.out.println(LangHelper.get("log.musicmanager.music.playing", id, volume));
        //System.out.println("[PokeTunes AutoPlay] Lecture de : " + id + " (volume max : " + volume + ")");
        //System.out.println("[PokeTunes AutoPlay] Playback of : " + id + " (max volume : " + volume + ")");

        currentMusic = selected;

        player.play(currentMusic, ConfigManager.fadeIn);
        if (shouldAutoPauseNextTrack) {
            PokeTunesAutoPlay.togglePaused(); // on remet la musique en pause / we pause the music again
            shouldAutoPauseNextTrack = false;
            forceNextTrack = false;
        }
        addToHistory(currentCategory, currentMusic);
    }

    /**
     * Quand une musique est jouée, elle est ajoutée à l'historique.
     * When a music is launched, it's added to history.
     * @param category
     * @param track
     */
    private void addToHistory(String category, PokeTunesMusic track) {
        LinkedList<PokeTunesMusic> history = playHistory.computeIfAbsent(category, k -> new LinkedList<>());

        if (history.contains(track)) {
            // On vire l’ancienne position / old position removal
            history.remove(track);
        }
        // Ajoute en tête (dernier joué)
        // Adding the last played music at first position
        history.addFirst(track);

        // Si on dépasse la limite : on vire le plus ancien
        // if the limit is passed : we delete the oldest music
        int maxSize = maxRecentTracks.getOrDefault(category, 3); // sécurité : 3 par défaut / 3 by default, by security
        while (history.size() > maxSize) {
            history.removeLast();
        }
    }

    public String getCurrentMusic() {
        if (player.isPlaying() && currentMusic != null) {
            return currentMusic.sourcePath;
        } else {
            return "Aucune.";
        }
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
    public String getCurrentCategoryFromWorld() {
        if (client.world == null || client.player == null) return "menu";
        if (client.world.getRegistryKey() == World.NETHER) return "nether";
        if (client.world.getRegistryKey() == World.END) return "end";
        // Pour la dimension Safari / For the Safari dimension
        if (client.world.getRegistryKey().getValue().equals(new Identifier("academy", "safari"))) return "safari";

        //overworld par défaut / Overworld by default
        return "game";
    }

    public void onWorldChanged(ClientWorld world) {
        boolean isNowInGame = world != null;
        if (player.isPaused()) {
            shouldAutoPauseNextTrack = true;
            forceNextTrack = true;
        }
        String newCategory = getCurrentCategoryFromWorld();
        playHistory.clear();
        if (player != null && player.isPlaying()) {
            if (wasInGame && !isNowInGame) {
                // En jeu → Menu
                player.stop(false);
            } else if (!wasInGame && isNowInGame) {
                // Menu → En jeu
                player.stop(false);
            } else if (isNowInGame) {
                // Changement de dimension (en jeu vers autre jeu)
                player.stop(true);
            } else {
                // world == null, et déjà en menu ? Peut arriver au tout début du jeu
                // -> On ignore !!!
            }
        }

        if (newCategory.equals("menu") || newCategory.equals("end")) {
            initialDelay = 40; // 2s
        } else if (!wasInGame && isNowInGame) {
            // Menu → En jeu
            initialDelay = 40; // 2s
        }/* else if (wasInGame && !isNowInGame) {
            // En jeu → Menu
            initialDelay = 40; // 2s
        }*/ else {
            // Changement de dimension (en jeu vers autre jeu)
            initialDelay = 200;
        }
        ticksUntilNextSong = initialDelay;
        justChangedContext = true;
        wasInGame = isNowInGame;
    }

    public List<PokeTunesMusic> getAvailableMusics() {
        return availableMusics;
    }

    public void markMenuReady() {
        this.menuReady = true;
        this.menuFirstLaunch = true;
        mustPlayFirstTrack = true ;
        justChangedContext = true;
        ticksUntilNextSong = 40; //2s
    }

    public void activateSoundSuppressor() {
        parasiteBlockTicks = 40 * 20; //40 secondes / 40 seconds
        SoundSuppressor.activate();
    }
}