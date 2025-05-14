package fr.titrouan.poketunesautoplay.mixin;

import fr.titrouan.poketunesautoplay.PokeTunesAutoPlay;
import fr.titrouan.poketunesautoplay.config.LangHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Active le blocage temporaire des sons parasites lors d’un changement de dimension.
 * Activates temporary parasite sound blocking on dimension change.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setWorld", at = @At("HEAD"))
    private void poketunesautoplay$onWorldChange(ClientWorld world, CallbackInfo ci) {
        if (PokeTunesAutoPlay.musicManager != null) {
            System.out.println(LangHelper.get("log.minecraftclient.suppressor.activate"));
            PokeTunesAutoPlay.musicManager.activateSoundSuppressor();

            PokeTunesAutoPlay.musicManager.onWorldChanged(world);
        }
    }
}