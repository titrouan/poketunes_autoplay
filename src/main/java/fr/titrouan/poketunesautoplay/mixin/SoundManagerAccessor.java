package fr.titrouan.poketunesautoplay.mixin;

import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SoundManager.class)
public abstract class SoundManagerAccessor {

    @Unique private boolean poketunesautoplay$isMusicPlaying;

    @Unique
    public boolean poketunesautoplay$isMusicPlayingBridge() {
        return poketunesautoplay$isMusicPlaying;
    }

    @Unique
    public void poketunesautoplay$resetMusicStatusBridge() {
        this.poketunesautoplay$isMusicPlaying = false;
    }

    // Tu peux aussi exposer une méthode pour modifier l’état :
    @Unique
    public void poketunesautoplay$setMusicPlayingBridge(boolean value) {
        this.poketunesautoplay$isMusicPlaying = value;
    }
}