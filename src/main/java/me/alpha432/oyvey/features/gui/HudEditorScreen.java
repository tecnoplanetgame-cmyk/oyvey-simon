package me.alpha432.oyvey.features.gui;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.gui.items.buttons.ModuleButton;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.HudModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

public class HudEditorScreen extends Screen {
    private static HudEditorScreen INSTANCE;
    private final ArrayList<Widget> components = new ArrayList<>();
    public HudModule currentDragging;
    public boolean anyHover;

    // Variabile per l'animazione arcobaleno
    private long startTime = System.currentTimeMillis();

    private HudEditorScreen() {
        super(Component.literal("oyvey-hudeditor"));
        load();
    }

    private void load() {
        Widget hud = new Widget("Hud", 50, 50, true);
        OyVey.moduleManager.stream()
                .filter(m -> m.getCategory() == Module.Category.HUD && !m.hidden)
                .map(ModuleButton::new)
                .forEach(hud::addButton);
        this.components.add(hud);
        this.components.forEach(component -> component.getItems().sort(Comparator.comparing(Feature::getName)));
    }

    /**
     * Calcola un colore arcobaleno animato basato sul tempo e su un offset (per effetto gradiente).
     * @param offset sfasamento in gradi (0-360) per creare l'effetto arcobaleno a cascata
     * @param alpha  trasparenza (0-255)
     */
    public int getRainbowColor(float offset, int alpha) {
        long elapsed = System.currentTimeMillis() - startTime;
        // Ciclo completo ogni 4 secondi
        float hue = ((elapsed % 4000) / 4000f + offset / 360f) % 1f;
        Color color = Color.getHSBColor(hue, 0.8f, 1.0f);
        return (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        anyHover = false;

        // Disegna un bordo arcobaleno animato intorno ad ogni widget
        for (Widget component : this.components) {
            drawRainbowBorder(context, component);
            component.drawScreen(context, mouseX, mouseY, delta);
        }
    }

    /**
     * Disegna un bordo arcobaleno a 4 righe intorno al widget, con colori sfasati.
     */
    private void drawRainbowBorder(GuiGraphics context, Widget component) {
        int x = component.getX();
        int y = component.getY();
        int w = component.getWidth();
        int h = component.getHeight();
        int thickness = 2;

        // Numero di segmenti per lato (più segmenti = gradiente più fluido)
        int segments = 20;

        // Lato superiore (sinistra → destra)
        for (int i = 0; i < segments; i++) {
            float offset = (i / (float) segments) * 60f;
            int color = getRainbowColor(offset, 255);
            int x1 = x + (i * w / segments);
            int x2 = x + ((i + 1) * w / segments);
            context.fill(x1, y, x2, y + thickness, color);
        }

        // Lato destro (alto → basso)
        for (int i = 0; i < segments; i++) {
            float offset = 60f + (i / (float) segments) * 60f;
            int color = getRainbowColor(offset, 255);
            int y1 = y + (i * h / segments);
            int y2 = y + ((i + 1) * h / segments);
            context.fill(x + w - thickness, y1, x + w, y2, color);
        }

        // Lato inferiore (destra → sinistra)
        for (int i = 0; i < segments; i++) {
            float offset = 120f + (i / (float) segments) * 60f;
            int color = getRainbowColor(offset, 255);
            int x1 = x + w - ((i + 1) * w / segments);
            int x2 = x + w - (i * w / segments);
            context.fill(x1, y + h - thickness, x2, y + h, color);
        }

        // Lato sinistro (basso → alto)
        for (int i = 0; i < segments; i++) {
            float offset = 180f + (i / (float) segments) * 60f;
            int color = getRainbowColor(offset, 255);
            int y1 = y + h - ((i + 1) * h / segments);
            int y2 = y + h - (i * h / segments);
            context.fill(x, y1, x + thickness, y2, color);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        this.components.forEach(component -> component.mouseClicked((int) click.x(), (int) click.y(), click.button()));
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.components.forEach(component -> component.mouseReleased((int) click.x(), (int) click.y(), click.button()));
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) {
            this.components.forEach(component -> component.setY(component.getY() - 10));
        } else if (verticalAmount > 0) {
            this.components.forEach(component -> component.setY(component.getY() + 10));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        this.components.forEach(component -> component.onKeyPressed(input.input()));
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        this.components.forEach(component -> component.onKeyTyped(input.codepointAsString(), input.modifiers()));
        return super.charTyped(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // nessuno sfondo, si vede il gioco dietro
    }

    public ArrayList<Widget> getComponents() {
        return components;
    }

    public static HudEditorScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HudEditorScreen();
        }
        return INSTANCE;
    }
}
