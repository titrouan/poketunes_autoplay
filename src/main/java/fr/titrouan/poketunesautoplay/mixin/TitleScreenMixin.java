package fr.titrouan.poketunesautoplay.mixin;

import fr.titrouan.poketunesautoplay.PokeTunesAutoPlay;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    private boolean poketunesautoplay$menuDisplayed = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void onMenuVisible(CallbackInfo ci) {
        if (!poketunesautoplay$menuDisplayed) {
            poketunesautoplay$menuDisplayed = true;
            //System.out.println("[PokeTunes AutoPlay] Le menu principal est maintenant visible !");
            //System.out.println("[PokeTunes AutoPlay] Main menu is now visible !");
            PokeTunesAutoPlay.musicManager.markMenuReady();
        }
    }
}
