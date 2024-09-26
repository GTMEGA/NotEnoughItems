package codechicken.nei.recipe;

import net.minecraft.client.gui.inventory.GuiContainer;

public class GuiScrollPaneViewport {
    private final GuiContainer gui;
    public int x;
    public int y;
    public int width;
    public int height;
    public GuiScrollPaneViewport(GuiContainer gui) {
        this.gui = gui;
    }

    public boolean isInViewportScreenSpace(int x, int y) {
        return isInViewportWindowSpace(x - gui.guiLeft, y - gui.guiTop);
    }
    public boolean isInViewportWindowSpace(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }
}
