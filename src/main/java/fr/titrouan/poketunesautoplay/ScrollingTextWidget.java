package fr.titrouan.poketunesautoplay;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

public class ScrollingTextWidget implements Drawable, Element, Selectable {

    private final TextRenderer textRenderer;
    private final String prefix;
    private String message;
    private int x, y;
    private int tickCounter = 0;
    private int scrollOffset = 0;
    private final int visibleChars;
    private static final int SCROLL_INTERVAL = 20; // 20 ticks = 1s
    private static final int PAUSE_DURATION = 60; // 3 secondes
    private float scrollPixelOffset = 0.0f;
    private static final float SCROLL_SPEED = 0.5f; // en pixels/tick

    public ScrollingTextWidget(TextRenderer textRenderer, String prefix, String fullText, int x, int y, int visibleChars) {
        this.textRenderer = textRenderer;
        this.prefix = prefix;
        this.message = fullText;
        this.x = x;
        this.y = y;
        this.visibleChars = visibleChars;
    }

    public void setText(String fullText) {
        if (!this.message.equals(fullText)) {
            this.message = fullText;
            this.scrollOffset = 0;
            this.scrollPixelOffset = 0.0f;
            this.tickCounter = 0;
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void tick() {
        int messageWidth = textRenderer.getWidth(message);
        int visibleWidth = textRenderer.getWidth("W".repeat(visibleChars));

        // Si le texte est plus court que la zone visible, pas de scroll
        // If the text is shorter (or equals) than visible zone, no scroll
        if (messageWidth <= visibleWidth) return;

        if (scrollPixelOffset == 0 && tickCounter < PAUSE_DURATION) {
            tickCounter++;
            return; // pause initiale uniquement / only inital pause
        }

        scrollPixelOffset += SCROLL_SPEED;

        // Reset à 0 si le texte a tout défilé (pas de pause finale)
        // Reset to 0 if the entire text has been shown (no final pause)
        if (scrollPixelOffset > messageWidth) {
            scrollPixelOffset = 0;
            tickCounter = 0;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();

        float scale = 0.75f;
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        int prefixWidth = textRenderer.getWidth(prefix);
        int clipWidth = textRenderer.getWidth("W".repeat(visibleChars));

        // 1. Affiche le préfixe
        context.drawTextWithShadow(textRenderer, prefix, 0, 0, 0xFFFFFF);

        // 2. Scissor box pour masquer le texte dépassant
        int scaledX = (int) (x + prefixWidth * scale);
        int scaledY = (int) (y);
        int scaledClipWidth = (int) (clipWidth * scale);
        int scaledHeight = (int) (textRenderer.fontHeight * scale);

        context.enableScissor(scaledX, scaledY, scaledX + scaledClipWidth, scaledY + scaledHeight);
        context.getMatrices().push();
        context.getMatrices().translate(prefixWidth - scrollPixelOffset, 0, 0);
        //context.drawTextWithShadow(textRenderer, message, 0, 0, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal(message).styled(style -> style.withColor(0x55FF55)), 0, 0, 0xFFFFFF);
        context.getMatrices().pop();
        context.disableScissor();

        context.getMatrices().pop();
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE; // Car ce n'est pas un élément interactif / it's not an interactive element
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        // Rien à faire ici / nothing to do here
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // Pas nécessaire pour ce widget / Not necessary for this widget
    }
}