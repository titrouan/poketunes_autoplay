package fr.titrouan.poketunesautoplay;

import com.google.gson.*;
import fr.titrouan.poketunesautoplay.config.LangHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.*;

/**
 * Charge les musiques des catégories depuis le sounds.json du resource pack.
 * Loads category entries from the sounds.json of the resource pack.
 */
public class PokeTunesSoundLoader {

    /**
     * Lit et extrait les musiques des catégories depuis le sounds.json actif.
     * Reads and extracts category music entries from the active sounds.json.
     */
    public static List<PokeTunesMusic> loadGameMusics() {
        List<PokeTunesMusic> musics = new ArrayList<>();


        try {
            MinecraftClient client = MinecraftClient.getInstance();
            Identifier soundsJsonId = new Identifier("poketunes", "sounds.json");
            InputStream stream = client.getResourceManager().getResource(soundsJsonId).get().getInputStream();

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String key = entry.getKey();
                // Ignore les balises visuelles comme "_category_game", si on veut en mettre.
                // Ignore visual tags such as "_category_game", if you want to put them. It's commentaries in json files.
                if (key.startsWith("_")) continue;

                try {
                    Identifier soundId = new Identifier(key);
                    JsonObject soundDef = entry.getValue().getAsJsonObject();
                    JsonArray soundArray = soundDef.getAsJsonArray("sounds");

                    for (JsonElement element : soundArray) {
                        JsonObject soundObj = element.getAsJsonObject();
                        String name = soundObj.get("name").getAsString();
                        float volume = soundObj.has("volume") ? soundObj.get("volume").getAsFloat() : 1.0f;
                        int weight = soundObj.has("weight") ? soundObj.get("weight").getAsInt() : 100;

                        musics.add(new PokeTunesMusic(soundId, name, volume, weight));
                        //System.out.println(LangHelper.get("log.soundloader.music.loaded", soundId, name, volume, weight));
                    }
                } catch (Exception e) {
                    System.err.println(LangHelper.get("log.soundloader.error.entry.parsing", key));
                    e.printStackTrace();
                }
            }

            System.out.println(LangHelper.get("log.soundloader.loadedmusics.getnumber", musics.size()));

        } catch (Exception e) {
            System.err.println(LangHelper.get("log.soundloader.error.soundsjson.load"));
            e.printStackTrace();
        }

        return musics;
    }
}