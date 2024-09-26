package codechicken.nei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import codechicken.nei.recipe.GuiScrollPaneViewport;
import org.lwjgl.opengl.GL11;

public class GuiNEIButton extends GuiButton
{
    protected static final ResourceLocation guiTex = new ResourceLocation("textures/gui/widgets.png");

    private final GuiScrollPaneViewport scroll;
    public boolean bypassScissor = false;

    public GuiNEIButton(int i, int j, int k, int l, int i1, String s, GuiScrollPaneViewport scroll)
    {
        super(i, j, k, l, i1, s);
        this.scroll = scroll;
    }
    public GuiNEIButton(int i, int j, int k, int l, int i1, String s)
    {
        super(i, j, k, l, i1, s);
        scroll = null;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (scroll != null) {
            if (!scroll.isInViewportScreenSpace(mouseX, mouseY))
                return false;
        }
        return super.mousePressed(mc, mouseX, mouseY);
    }

    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        if(!visible)
            return;
        if (bypassScissor) {
            GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        try {

            boolean allowed = true;
            if (scroll != null) {
                allowed = scroll.isInViewportScreenSpace(mouseX, mouseY);
            }

            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.renderEngine.bindTexture(guiTex);
            GL11.glColor4f(1, 1, 1, 1);
            boolean mouseOver = allowed && mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            int k = getHoverState(mouseOver);
            drawTexturedModalRect(xPosition, yPosition, 0, 46 + k * 20, width / 2, height / 2);//top left
            drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, 46 + k * 20, width / 2, height / 2);//top right
            drawTexturedModalRect(xPosition, yPosition + height / 2, 0, 46 + k * 20 + 20 - height / 2, width / 2, height / 2);//bottom left
            drawTexturedModalRect(xPosition + width / 2, yPosition + height / 2, 200 - width / 2, 46 + k * 20 + 20 - height / 2, width / 2, height / 2);//bottom right
            mouseDragged(minecraft, mouseX, mouseY);

            if (!enabled)
                drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffa0a0a0);
            else if (mouseOver)
                drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffffa0);
            else
                drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xe0e0e0);
        } finally {
            if (bypassScissor) {
                GL11.glPopAttrib();
            }
        }
    }
}
