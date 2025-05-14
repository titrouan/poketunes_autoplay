package fr.titrouan.poketunesautoplay.access;

public interface OptionsScreenMixinBridge {
    void poketunesautoplay$updateCurrentTrack(String name);
    void poketunesautoplay$tickScrollingText();
    void poketunesautoplay$updatePlayPauseIcon(boolean isPaused);
}