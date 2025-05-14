package fr.titrouan.poketunesautoplay.mixin;

import fr.titrouan.poketunesautoplay.config.LangHelper;
import fr.titrouan.poketunesautoplay.sound.SoundSuppressor;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepte la lecture de sons parasites et les bloque si nécessaire.
 * Intercepts unwanted sounds and blocks them if needed.
 */
@Mixin(SoundManager.class)
public class SoundManagerBlockerMixin {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void poketunesautoplay$blockUnwantedSounds(SoundInstance soundInstance, CallbackInfo ci) {
        Identifier id = soundInstance.getId();
        if (id != null && SoundSuppressor.shouldBlock(id)) {
            //System.out.println(LangHelper.get("log.soundmanagermixin.extraneous.block", id));
            ci.cancel();
        }
    }
}