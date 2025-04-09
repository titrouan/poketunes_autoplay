package fr.titrouan.poketunesautoplay.fade;

import fr.titrouan.poketunesautoplay.access.VolumeAdjustable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.math.MathHelper;

import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicFadesManager {

    private MinecraftClient client;
    private SoundInstance currentSoundInstance;
    private float targetVolumeSupplier;
    private int fadeInTicks;
    private int fadeInCounter;
    private boolean isFadingIn;
    private final Random random = Random.create(); // Création d'une instance Random

    public MusicFadesManager(MinecraftClient client) {
        this.client = client;

        // Enregistre le tick client pour appliquer les fondus à chaque tick
        // Register client tick to update fades on every tick
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    /**
     * Démarre le fondu d'entrée d'une musique.
     * Starts the fade-in effect for a music instance.
     */
    public void startFadeIn(SoundInstance soundInstance, int fadeInTicks, float volumeCible) {
        System.out.println("[PokeTunes AutoPlay] Début du fondu d'entrée pour " + soundInstance.getId());
        this.currentSoundInstance = soundInstance;
        this.fadeInTicks = fadeInTicks;
        this.fadeInCounter = 0;
        this.isFadingIn = true;
        this.targetVolumeSupplier = volumeCible;

        // Démarre la musique avec volume initial à 0
        // Start music at zero volume
        client.getSoundManager().play(currentSoundInstance);
        if (currentSoundInstance instanceof VolumeAdjustable adjustable) {
            adjustable.poketunesautoplay$setVolume(0.0f);
        }
    }

    /**
     * Gère le fondu d'entrée à chaque tick client.
     * Handles the fade-in progression on every client tick.
     */
    private void onClientTick(MinecraftClient client) {
        if (isFadingIn && currentSoundInstance != null) {
            fadeInCounter++;
            System.out.println("[PokeTunes AutoPlay] Augmentation du compteur de fondu d'entrée : " + fadeInCounter);

            if (fadeInCounter >= fadeInTicks) {
                // Fin du fondu d'entrée, on applique le volume final
                // Fade-in complete, apply target volume
                if (currentSoundInstance instanceof VolumeAdjustable adjustable) {
                     /* Même si on "donne" en paramètre un Random, si le volume est défini comme constant dans sounds.json,
                      * le FloatSupplier renverra toujours cette valeur fixe.
                      * Even though we pass a Random, if the volume is defined as a constant in sounds.json,
                      * the FloatSupplier will always return the same fixed value.
                     */
                    adjustable.poketunesautoplay$setVolume(targetVolumeSupplier); // Passer random comme argument
                    System.out.println("[PokeTunes AutoPlay] Fin du fondu, volume cible = " + targetVolumeSupplier);
                }
                isFadingIn = false;
            } else {
                // Interpolation linéaire du volume actuel
                // Linear interpolation of current volume
                float interpolatedVolume = MathHelper.lerp(
                        (float) fadeInCounter / fadeInTicks,
                        0.0f,
                        targetVolumeSupplier // Passer random comme argument
                );
                if (currentSoundInstance instanceof VolumeAdjustable adjustable) {
                    adjustable.poketunesautoplay$setVolume(interpolatedVolume);
                    System.out.println("[PokeTunes AutoPlay] Tick " + fadeInCounter + ", volume actuel = " + interpolatedVolume);
                }
            }
        }
    }
}