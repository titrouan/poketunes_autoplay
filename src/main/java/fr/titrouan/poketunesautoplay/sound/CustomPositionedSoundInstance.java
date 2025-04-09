/*package fr.titrouan.poketunesautoplay.sound;

import fr.titrouan.poketunesautoplay.access.VolumeAdjustable;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class CustomPositionedSoundInstance extends AbstractSoundInstance {
    public CustomPositionedSoundInstance(Identifier id, float pitch) {
        super(id, SoundCategory.MUSIC, Random.create());
        this.pitch = pitch;
        this.volume = 1.0f;
        this.attenuationType = AttenuationType.NONE;
        this.repeat = false;
        this.relative = true;
    }
}

*/
package fr.titrouan.poketunesautoplay.sound;

import fr.titrouan.poketunesautoplay.access.VolumeAdjustable;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import org.jetbrains.annotations.Nullable;

public class CustomPositionedSoundInstance implements TickableSoundInstance, VolumeAdjustable {
    private final Identifier id;
    private float pitch;
    private float volume;
    private AttenuationType attenuationType = AttenuationType.NONE;
    private boolean repeat = false;
    private boolean relative = true;

    private final Sound sound;

    public CustomPositionedSoundInstance(Identifier id, String soundName, float pitch) {
        this.id = id;
        this.sound = new Sound(
                soundName,                                  // Nom du son (chemin vers le .ogg, sans extension)
                ConstantFloatProvider.create(1.0f),         // volume
                ConstantFloatProvider.create(pitch),        // pitch
                1,                                          // Poids
                Sound.RegistrationType.FILE,                // Type d'enregistrement
                true,                                       // Stream
                false,                                      // preload
                0                                           // Atténuation (positionnel ou non)
        );
        this.pitch = pitch;
        this.volume = 1.0f;
        //System.out.println("[PokeTunes AutoPlay] Chemin de id : " + soundName);
        //System.out.println("[PokeTunes AutoPlay] Path of id : " + soundName);
    }

    @Override
    public void tick() {

    }

    @Override
    public @Nullable WeightedSoundSet getSoundSet(SoundManager soundManager) {
        // On crée dynamiquement un ensemble de sons contenant notre son unique
        // We dynamically create a set of sounds containing our unique sound
        return new WeightedSoundSet(id, null);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public SoundCategory getCategory() {
        return SoundCategory.MUSIC;
    }

    @Override
    public boolean isRepeatable() {
        return repeat;
    }

    @Override
    public boolean isRelative() {
        return relative;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public double getX() {
        return 0.0;
    }

    @Override
    public double getY() {
        return 0.0;
    }

    @Override
    public double getZ() {
        return 0.0;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return attenuationType;
    }

    @Override
    public boolean canPlay() {
        return true;
    }

    @Override
    public boolean isDone() {
         // Indique que le son n'est pas terminé
         // Indicates that the sound is not finished
        return false;
    }

@Override
public void poketunesautoplay$setVolume(float volume) {
    this.volume = volume;
    //System.out.println("[PokeTunes AutoPlay] Volume mis à jour : " + volume);
    //System.out.println("[PokeTunes AutoPlay] Volume updated : " + volume);
}
}