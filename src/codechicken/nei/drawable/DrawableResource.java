package codechicken.nei.drawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class DrawableResource  {

    private final ResourceLocation resourceLocation;
    private final int textureWidth;
    private final int textureHeight;

    private final int u;
    private final int v;
    private final int width;
    private final int height;
    private final int paddingTop;
    private final int paddingBottom;
    private final int paddingLeft;
    private final int paddingRight;

    public DrawableResource(ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight, int textureWidth, int textureHeight) {
        this.resourceLocation = resourceLocation;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;

        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }

    public int getWidth() {
        return width + paddingLeft + paddingRight;
    }

    public int getHeight() {
        return height + paddingTop + paddingBottom;
    }

    public void draw(int xOffset, int yOffset) {
        draw(xOffset, yOffset, 0, 0, 0, 0);
    }

    public void draw(int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.resourceLocation);

        int x = xOffset + this.paddingLeft + maskLeft;
        int y = yOffset + this.paddingTop + maskTop;
        int u = this.u + maskLeft;
        int v = this.v + maskTop;
        int width = this.width - maskRight - maskLeft;
        int height = this.height - maskBottom - maskTop;
        // drawModalRectWithCustomSizedTexture
        Gui.func_146110_a(x, y, u, v, width, height, textureWidth, textureHeight);
    }
}

