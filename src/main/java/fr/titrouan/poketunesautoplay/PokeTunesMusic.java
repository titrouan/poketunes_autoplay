package fr.titrouan.poketunesautoplay;

import net.minecraft.util.Identifier;

/**
 * Représente une musique issue du fichier sounds.json.
 * Represents a music entry from the sounds.json file.
 */
public class PokeTunesMusic {
    public final Identifier id; // "poketunes:xxx"
    public final String sourcePath; // "poketunes:music/game/xxx"
    public String realPath; // "sounds/music/game/xxx.ogg"
    public final float volume;
    public final int weight;

    public PokeTunesMusic(Identifier id, String sourcePath, float volume, int weight) {
        this.id = id;
        this.sourcePath = sourcePath;
        this.volume = volume;
        this.weight = weight;

        String[] parts = sourcePath.split("/");
        if (parts.length >= 3 && parts[0].equals("poketunes:music")) {
            this.realPath = "music/" + parts[1] + "/" + parts[2] + ".ogg";
        }
    }

    public String getCategory() {
        // Exemple : "poketunes:music/game/accumula_town" → "game"
        String[] parts = sourcePath.split("/");
        if (parts.length >= 3 && parts[0].endsWith("poketunes:music")) {
            return parts[1]; // "game", "menu", etc.
        }
        if (parts.length >= 3 && parts[0].equals("poketunes:music")) {
            return parts[1];
        }
        if (parts.length >= 3) {
            return parts[1]; // Fallback
        }
        return "unknown";
    }

    @Override
    public String toString() {
        return "PokeTunesMusic{id=" + id + ", volume=" + volume + ", weight=" + weight + '}';
    }
}