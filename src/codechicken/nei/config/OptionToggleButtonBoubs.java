package codechicken.nei.config;

import codechicken.lib.render.CCRenderState;
import codechicken.nei.Image;
import codechicken.nei.LayoutManager;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawStringC;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

public class OptionToggleButtonBoubs extends OptionButton
{
    public final boolean prefixed;

    public OptionToggleButtonBoubs(String name, boolean prefixed) {
        super(name);
        this.prefixed = prefixed;
    }

    public OptionToggleButtonBoubs(String name) {
        this(name, false);
    }

    public boolean state() {
        return renderTag().getBooleanValue();
    }

    @Override
    public void drawButton(int mx, int my) {
        Rectangle b = buttonSize();
        LayoutManager.drawButtonBackground(b.x, b.y, b.width, b.height, true, getButtonTex(mx, my));
        if(state()) {
            changeTexture("nei:textures/nei_sprites.png");
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Image image = new Image(48, 52, 24, 24);
//            drawTexturedModalRect(x, y, image.x, image.y, image.width, image.height);
            drawTexturedModalRect(b.x + (b.width/2) - (image.width/2), b.y, image.x, image.y, image.width, image.height);
            GL11.glDisable(GL11.GL_BLEND);

        }else {
            drawStringC(getButtonText(), b.x, b.y, b.width, b.height, getTextColour(mx, my));
        }
    }
    
    public String getButtonText() {
        return translateN(name + (state() ? ".true" : ".false"));
    }

    @Override
    public String getPrefix() {
        return prefixed ? translateN(name) : null;
    }

    @Override
    public boolean onClick(int button) {
        getTag().setBooleanValue(!state());
        return true;
    }
}
