package fr.titrouan.poketunesautoplay;

import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.*;

/**
 * Charge les musiques de la catégorie "music.game" depuis le sounds.json du resource pack.
 * Loads "music.game" entries from the sounds.json of the resource pack.
 */
public class PokeTunesSoundLoader {

    /**
     * Lit et extrait les musiques de la catégorie "music.game" depuis le sounds.json actif.
     * Reads and extracts "music.game" music entries from the active sounds.json.
     */
    public static List<PokeTunesMusic> loadGameMusics() {
        List<PokeTunesMusic> musics = new ArrayList<>();

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            Identifier soundsJsonId = new Identifier("minecraft", "sounds.json");
            InputStream stream = client.getResourceManager().getResource(soundsJsonId).get().getInputStream();

            /*code temporaire
            Optional<Resource> resourceOptional = client.getResourceManager().getResource(soundsJsonId);
            if (resourceOptional.isEmpty()) {
                System.err.println("[PokeTunes AutoPlay] sounds.json introuvable dans le resource manager !");
                return musics;
            }
            InputStream stream = resourceOptional.get().getInputStream();*/


            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

            if (!root.has("music.game")) {
                System.err.println("[PokeTunes AutoPlay] Aucune entrée 'music.game' trouvée dans sounds.json / No 'music.game' entry found in sounds.json");
                return musics;
            }

            JsonObject musicGame = root.getAsJsonObject("music.game");
            JsonArray sounds = musicGame.getAsJsonArray("sounds");

            for (JsonElement element : sounds) {
                JsonObject soundObj = element.getAsJsonObject();

                String name = soundObj.get("name").getAsString();
                float volume = soundObj.has("volume") ? soundObj.get("volume").getAsFloat() : 1.0f;
                int weight = soundObj.has("weight") ? soundObj.get("weight").getAsInt() : 100;

                Identifier soundId = new Identifier(name);
                musics.add(new PokeTunesMusic(soundId, volume, weight));
            }

            System.out.println("[PokeTunes AutoPlay] " + musics.size() + " musiques chargées depuis 'music.game' / musics loaded from 'music.game'");

        } catch (Exception e) {
            System.err.println("[PokeTunes AutoPlay] Erreur lors du chargement de sounds.json / Error loading sounds.json:");
            e.printStackTrace();
        }

        return musics;
    }
}