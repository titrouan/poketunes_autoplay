package fr.titrouan.poketunesautoplay.audio;

import fr.titrouan.poketunesautoplay.PokeTunesMusic;
import fr.titrouan.poketunesautoplay.config.ConfigManager;
import fr.titrouan.poketunesautoplay.config.LangHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PokeTunesAudioPlayer {

    private int source = -1;
    private int buffer = -1;

    private float targetVolume = 1.0f;
    private int fadeTicksRemaining = 0;
    private int fadeOutTicksRemaining = 0;
    private float fadeOutStartGain = 0.5f;
    private float fadeOutStep = 0.0f;

    private int savedFadeTicksRemaining = 0;

    private boolean isPlaying = false;
    private boolean isFadingOut = false;

    private boolean isPaused = false;
    private float pausedGain = 0.0f;

    public void play(PokeTunesMusic sound, int fadeDurationInTicks) {
        // Stop ce qui joue déjà / Stop anything already playing
        stop(false);

        try {
            /**
             * 1. Trouver le resource pack PokeTunes actif
             * 1. Find the active PokeTunes resource pack
             */
            File resourcePacksDir = new File(MinecraftClient.getInstance().runDirectory, "resourcepacks");
            File pokeTunesPack = new File(resourcePacksDir, "PokeTunes.zip"); // Nom à adapter si besoin / Adapt the name if you need

            if (!pokeTunesPack.exists()) {
                System.err.println(LangHelper.get("log.player.error.rp.notfound",pokeTunesPack.getAbsolutePath()));
                return;
            }

            /**
             * 2. Construire le chemin vers le fichier ogg
             * 2. Build path to ogg file
             */
            String pathInZip = "assets/poketunes/sounds/music/" + sound.sourcePath.replace("poketunes:music/", "") + ".ogg";

            /**
             * 3. Lire le fichier depuis le zip
             * 3. Read the file from zip
             */
            try (ZipFile zipFile = new ZipFile(pokeTunesPack)) {
                ZipEntry entry = zipFile.getEntry(pathInZip);
                if (entry == null) {
                    System.err.println(LangHelper.get("log.player.error.rp.oggfile.notfound", pathInZip));
                    return;
                }

                InputStream inputStream = zipFile.getInputStream(entry);
                byte[] oggData = inputStream.readAllBytes();
                ByteBuffer vorbis = MemoryUtil.memAlloc(oggData.length);
                vorbis.put(oggData).flip();

                IntBuffer error = MemoryUtil.memAllocInt(1);
                long decoder = STBVorbis.stb_vorbis_open_memory(vorbis, error, null);
                if (decoder == MemoryUtil.NULL) {
                    System.err.println(LangHelper.get("log.player.error.rp.oggfile.play", error.get(0)));
                    MemoryUtil.memFree(vorbis);
                    return;
                }

                STBVorbisInfo info = STBVorbisInfo.malloc();
                STBVorbis.stb_vorbis_get_info(decoder, info);

                int channels = info.channels();
                int sampleRate = info.sample_rate();

                int samples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
                ShortBuffer pcm = MemoryUtil.memAllocShort(samples * channels);
                STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);

                STBVorbis.stb_vorbis_close(decoder);
                info.free();

                buffer = AL10.alGenBuffers();
                AL10.alBufferData(buffer, channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, pcm, sampleRate);
                MemoryUtil.memFree(pcm);
                MemoryUtil.memFree(vorbis);
                MemoryUtil.memFree(error);

                source = AL10.alGenSources();
                AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
                AL10.alSourcef(source, AL10.AL_GAIN, 0.0f);
                AL10.alSourcePlay(source);

                targetVolume = sound.volume;
                fadeTicksRemaining = fadeDurationInTicks;
                isPlaying = true;

                String[] zipSplit = pathInZip.split("/");
                System.out.println(LangHelper.get("log.player.reading", zipSplit[5]));
            }
        } catch (Exception e) {
            System.err.println(LangHelper.get("log.player.error.rp.read", e.getMessage()));
            e.printStackTrace();
        }
    }

    public boolean togglePause() {
        if (!isPlaying || source == -1) return false;
        if (isFadingOut) {
            //Impossible de mettre en pause pendant un fondu de sortie
            //Impossible to pause music during a fade-out
            return false;
        }

        if (isPaused) {
            // Reprise / music returns
            AL10.alSourcePlay(source);
            // Restaurer le fondu / restore the fade
            fadeTicksRemaining = savedFadeTicksRemaining;
            isPaused = false;
        } else {
            // Pause
            pausedGain = AL10.alGetSourcef(source, AL10.AL_GAIN);
            // Sauver le fondu restant / save remaining time of the fade
            savedFadeTicksRemaining = fadeTicksRemaining;
            AL10.alSourcePause(source);
            isPaused = true;
        }
        //switch visuel autorisé / visual switch authorized
        return true;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void stop(boolean withFadeOut) {
        if (!withFadeOut) {
            if (isPlaying) {
                if (AL10.alIsSource(source)) {
                    AL10.alSourceStop(source);
                    AL10.alDeleteSources(source);
                }
                if (AL10.alIsBuffer(buffer)) {
                    AL10.alDeleteBuffers(buffer);
                }
                source = -1;
                buffer = -1;
                isPlaying = false;
                isPaused = false;
            }
        }

        if (withFadeOut) {
            if (isPlaying && AL10.alIsSource(source)) {
                isFadingOut = true;
                fadeOutTicksRemaining = ConfigManager.fadeOut;
                fadeOutStartGain = AL10.alGetSourcef(source, AL10.AL_GAIN);
                fadeOutStep = fadeOutStartGain / fadeOutTicksRemaining;
            }
            isPaused = false;
        }
    }

    public void tick() {
        if (!isPlaying || isPaused) return;

        MinecraftClient client = MinecraftClient.getInstance();

        boolean paused = client.isPaused();
        float musicVolume = client.options.getSoundVolume(SoundCategory.MUSIC); // Slider MUSIC
        float masterVolume = client.options.getSoundVolume(SoundCategory.MASTER); // Slider MASTER
        float volumeMultiplier = musicVolume * masterVolume;

        float gain;

        if (paused) {
            gain = 0.0f; // Mute total si en pause / Total mute if paused
        } else if (fadeTicksRemaining > 0) {
            // Calcul du fondu / Fade-in calculation
            float currentGain = AL10.alGetSourcef(source, AL10.AL_GAIN);
            float step = targetVolume * volumeMultiplier / fadeTicksRemaining;

            gain = currentGain + step;
            fadeTicksRemaining--;

            if (gain >= targetVolume * volumeMultiplier || fadeTicksRemaining <= 0) {
                gain = targetVolume * volumeMultiplier;
                fadeTicksRemaining = 0;
            }
        } else {
            // Volume normal si pas en pause ni fondu
            // Normal volume if not paused or faded
            gain = targetVolume * volumeMultiplier;
        }

        AL10.alSourcef(source, AL10.AL_GAIN, gain);

        if (isFadingOut && AL10.alIsSource(source)) {
            float newGain = fadeOutStartGain - fadeOutStep * (ConfigManager.fadeOut - fadeOutTicksRemaining);

            fadeOutTicksRemaining--;

            if (newGain <= 0.01f || fadeOutTicksRemaining <= 0) {
                AL10.alSourcef(source, AL10.AL_GAIN, 0.0f);
                AL10.alSourceStop(source);
                AL10.alDeleteSources(source);
                AL10.alDeleteBuffers(buffer);
                source = -1;
                buffer = -1;
                isPlaying = false;
                isFadingOut = false;
                fadeOutTicksRemaining = 0;
            } else {
                AL10.alSourcef(source, AL10.AL_GAIN, newGain);
            }
            return;
        }
    }

    public boolean isPlaying() {
        if (!isPlaying || source == -1) return false;

        int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        if (state != AL10.AL_PLAYING && state != AL10.AL_PAUSED) {
            stop(false); // Nettoyage / cleaning
            return false;
        }
        return true;
    }

    public float getPlaybackVolume() {
        if (source == -1) return 0.0f;
        return AL10.alGetSourcef(source, AL10.AL_GAIN);
    }
}