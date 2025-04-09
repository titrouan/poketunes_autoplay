package fr.titrouan.poketunesautoplay.mixin;

import fr.titrouan.poketunesautoplay.access.VolumeAdjustable;
import net.minecraft.client.sound.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSoundInstance.class)
public class SoundInstanceMixin implements VolumeAdjustable {

    @Unique
    private float poketunesautoplay$volumeOverride = -1f;

    @Override
    public void poketunesautoplay$setVolume(float volume) {
        this.poketunesautoplay$volumeOverride = volume;
        //System.out.println("[PokeTunes AutoPlay] Volume mis à jour : " + volume);
        //System.out.println("[PokeTunes AutoPlay] Volume updated : " + volume);
    }

    @Inject(method = "getVolume", at = @At("HEAD"), cancellable = true)
    private void onGetVolume(CallbackInfoReturnable<Float> cir) {
        System.out.println("[PokeTunes AutoPlay] Méthode getVolume appelée");
        if (poketunesautoplay$volumeOverride >= 0f) {
            cir.setReturnValue(poketunesautoplay$volumeOverride);
        }
    }
}