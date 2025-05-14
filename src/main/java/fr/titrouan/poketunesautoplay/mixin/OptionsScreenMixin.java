package fr.titrouan.poketunesautoplay.mixin;

import fr.titrouan.poketunesautoplay.PokeTunesAutoPlay;
import fr.titrouan.poketunesautoplay.ScrollingTextWidget;
import fr.titrouan.poketunesautoplay.access.OptionsScreenMixinBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin pour ajouter à l'écran des options un bouton play/pause et l'info de la musique en cours.
 * Mixin to add a play/pause button and the current music title that is playing.
 */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen implements OptionsScreenMixinBridge {

    protected OptionsScreenMixin(Text title) {
        super(title); // Requis pour le mixin sur Screen / Needed for the mixin on Screen
    }

    private ButtonWidget playPauseButton;
    private TextWidget currentMusicText;
    private ScrollingTextWidget scrollingText;

    @Inject(method = "init", at = @At("TAIL"))
    private void poketunesautoplay$addMusicControls(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        int screenWidth = this.width;
        int screenHeight = this.height;

        int buttonWidth = 20;
        int buttonHeight = 20;
        int pauseHeight = 26;
        int horizontalOffset = 5;//10

        int targetY = screenHeight / 4 + 48 - (pauseHeight*2) - 2;

        int buttonX = screenWidth / 2 + 155 - buttonWidth - horizontalOffset;

        String currentTrack = (PokeTunesAutoPlay.musicManager != null && PokeTunesAutoPlay.musicManager.getCurrentMusic() != null)
                ? PokeTunesAutoPlay.musicManager.getCurrentMusic().replace("poketunes:music/", "")
                : "Aucune.";

        Text text = Text.literal("Musique en cours : " + currentTrack);
        int textWidth = client.textRenderer.getWidth(text);
        int textX = (buttonX -120) - /*textWidth*/31 - 6;
        int textY = targetY + (buttonHeight - 6);

        // Scrolling text
        scrollingText = new ScrollingTextWidget(
                client.textRenderer,
                "Musique en cours : ",
                currentTrack,
                textX,
                textY,
                17
        );
        this.addDrawableChild(scrollingText);

        //tempo.
        TextWidget textWidget = new TextWidget(text, client.textRenderer);
        textWidget.setPosition(textX, textY);
        //this.addDrawableChild(textWidget);
        this.currentMusicText = textWidget;

        String icon = PokeTunesAutoPlay.isPaused() ? "▶" : "⏸";
        ButtonWidget playPause = ButtonWidget.builder(Text.of(icon), b -> {
            boolean toggled = PokeTunesAutoPlay.togglePaused();
            if (toggled) {
                b.setMessage(Text.of(PokeTunesAutoPlay.isPaused() ? "▶" : "⏸"));
            }
        }).dimensions(buttonX, targetY, buttonWidth, buttonHeight).build();

        this.addDrawableChild(playPause);
        this.playPauseButton = playPause;
    }

    public void poketunesautoplay$tickScrollingText() {
        if (scrollingText != null) {
            scrollingText.tick();
        }
    }

    @Override
    public void poketunesautoplay$updateCurrentTrack(String trackName) {
        if (currentMusicText != null) {
            String name = trackName.replace("poketunes:music/", "");
            Text newText = Text.literal("Musique en cours : " + name);
            scrollingText.setText(name);
            currentMusicText.setMessage(newText);
        }
    }

    @Override
    public void poketunesautoplay$updatePlayPauseIcon(boolean isPaused) {
        if (playPauseButton != null) {
            playPauseButton.setMessage(Text.of(isPaused ? "▶" : "⏸"));
        }
    }
}