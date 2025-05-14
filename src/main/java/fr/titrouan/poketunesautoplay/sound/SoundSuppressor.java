package fr.titrouan.poketunesautoplay.sound;

import fr.titrouan.poketunesautoplay.config.LangHelper;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Gère temporairement le blocage des sons parasites (ex : portails).
 * Temporarily blocks parasite sounds (e.g. portals).
 */
public class SoundSuppressor {

    private static final Set<Identifier> BLOCKED_SOUNDS = new HashSet<>();
    private static boolean active = false;

    static {
        BLOCKED_SOUNDS.add(new Identifier("minecraft", "block.portal.travel"));
        BLOCKED_SOUNDS.add(new Identifier("minecraft", "block.portal.ambient"));
        BLOCKED_SOUNDS.add(new Identifier("minecraft", "block.portal.trigger"));
        BLOCKED_SOUNDS.add(new Identifier("minecraft", "block.portal.spawn"));
        BLOCKED_SOUNDS.add(new Identifier("minecraft", "block.end_portal.spawn"));
        //BLOCKED_SOUNDS.add(new Identifier("minecraft", "entity.enderman.teleport"));
        //... (other wanted blocked sounds)
    }

    public static void activate() {
        active = true;
        System.out.println(LangHelper.get("log.soundsuppressor.extraneoussounds.blocked"));
    }

    public static void deactivate() {
        active = false;
        System.out.println(LangHelper.get("log.soundsuppressor.extraneoussounds.unblocked"));
    }

    public static boolean shouldBlock(Identifier id) {
        return active && BLOCKED_SOUNDS.contains(id);
    }
}