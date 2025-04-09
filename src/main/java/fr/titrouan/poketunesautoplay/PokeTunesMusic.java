package fr.titrouan.poketunesautoplay;

import net.minecraft.util.Identifier;

/**
 * Représente une musique issue du fichier sounds.json.
 * Represents a music entry from the sounds.json file.
 */
public class PokeTunesMusic {
    public final Identifier id;
    public final float volume;
    public final int weight;

    public PokeTunesMusic(Identifier id, float volume, int weight) {
        this.id = id;
        this.volume = volume;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "PokeTunesMusic{id=" + id + ", volume=" + volume + ", weight=" + weight + '}';
    }
}