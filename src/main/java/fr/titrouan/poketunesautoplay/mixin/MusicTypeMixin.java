package fr.titrouan.poketunesautoplay.mixin;

import fr.titrouan.poketunesautoplay.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//@Mixin(MinecraftClient.class)
public class MusicTypeMixin {
    //@Inject(method = "getMusicType", at = @At("HEAD"), cancellable = true)
    private void overrideMusicType(CallbackInfoReturnable<MusicSound> cir) {
        // Récupère les paramètres de configuration
        int minDelay = ConfigManager.minDelay;
        int maxDelay = ConfigManager.maxDelay;

        // Récupère le contexte du jeu (overworld, nether, etc.)
        MinecraftClient client = MinecraftClient.getInstance();

        RegistryEntry<SoundEvent> soundEvent = null;

        if (client.world != null && client.player != null) {
            /*if (client.world.getRegistryKey() == net.minecraft.world.World.NETHER) {
                soundEvent = SoundEvents.MUSIC_NETHER;
            } else if (client.world.getRegistryKey() == net.minecraft.world.World.END) {
                soundEvent = SoundEvents.MUSIC_END;
            } else {*/
            if (client.world.getRegistryKey() == World.OVERWORLD) {
                soundEvent = SoundEvents.MUSIC_GAME;
            }
        }/* else {
            soundEvent = SoundEvents.MUSIC_MENU;
        }*/

        if (soundEvent != null) { // if pour les tests avant les dimensions (garder contenu)
            cir.setReturnValue(new MusicSound(soundEvent, minDelay, maxDelay, false));
        }
    }
}