package fr.titrouan.poketunesautoplay.mixin;

import fr.titrouan.poketunesautoplay.access.SoundManagerMixinBridge;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SoundManager.class)
public abstract class SoundManagerMixin implements SoundManagerMixinBridge {

    // Notre variable pour suivre l’état (true si une musique est active)
    // Our variable to track the status (true if a music is active)
    @Unique
    private boolean poketunesautoplay$isMusicPlaying = false;

    /**
     * Lorsqu’un son est joué, on vérifie s’il s’agit d’une musique.
     * When a sound is played, check if it is a MUSIC type.
     */
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onPlay(SoundInstance soundInstance, CallbackInfo ci) {
        if (soundInstance.getCategory() == SoundCategory.MUSIC) {
            poketunesautoplay$isMusicPlaying = true;
        }
    }

    /**
     * Méthode d’accès à notre variable (sera appelée dans ton MusicManager)
     * Accessor for checking if a music is playing.
     */
    public boolean poketunesautoplay$isMusicPlaying() {
        return poketunesautoplay$isMusicPlaying;
    }

    /**
     * Méthode pour réinitialiser le statut, si besoin.
     * Reset music playing status manually if needed.
     */
    public void poketunesautoplay$resetMusicStatus() {
        poketunesautoplay$isMusicPlaying = false;
    }

    /**
     * Permet d’exposer proprement le test de lecture de musique sans faire appel direct au mixin d’accès.
     * Cleanly exposes music playback check without directly accessing accessor mixin.
     */
    @Unique
    @Override
    public boolean poketunesautoplay$isMusicPlayingBridge() {
        return this.poketunesautoplay$isMusicPlaying();
    }
}