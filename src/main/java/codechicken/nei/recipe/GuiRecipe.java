package codechicken.nei.recipe;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.GuiNEIButton;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.IGuiContainerOverlay;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.api.TaggedInventoryArea;
import codechicken.nei.drawable.DrawableBuilder;
import codechicken.nei.drawable.DrawableResource;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import codechicken.nei.guihook.IGuiClientSide;
import codechicken.nei.guihook.IGuiHandleMouseWheel;
import lombok.AllArgsConstructor;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

public abstract class GuiRecipe extends GuiContainer implements IGuiContainerOverlay, IGuiClientSide, IGuiHandleMouseWheel, IContainerTooltipHandler, INEIGuiHandler {
    // Background image calculations
    private static final int BG_TOP_HEIGHT = 6;
    private static final int BG_MIDDLE_HEIGHT = 154;
    private static final int BG_BOTTOM_HEIGHT = 6;
    
    private static final int BG_TOP_Y = 0;
    private static final int BG_MIDDLE_Y = BG_TOP_Y + BG_TOP_HEIGHT;
    private static final int BG_BOTTOM_Y = BG_MIDDLE_Y + BG_MIDDLE_HEIGHT;

    // Button sizes
    private static final int borderPadding = 6;
    private static final int buttonWidth = 13;
    private static final int buttonHeight = 12;

    // Background image
    final DrawableResource bgTop =    new DrawableBuilder("nei:textures/gui/recipebg.png", 0, BG_TOP_Y,    176, BG_TOP_HEIGHT).build();
    final DrawableResource bgMiddle = new DrawableBuilder("nei:textures/gui/recipebg.png", 0, BG_MIDDLE_Y, 176, BG_MIDDLE_HEIGHT).build();
    final DrawableResource bgBottom = new DrawableBuilder("nei:textures/gui/recipebg.png", 0, BG_BOTTOM_Y, 176, BG_BOTTOM_HEIGHT).build();
    
    public ArrayList<? extends IRecipeHandler> currenthandlers = new ArrayList<>();

    public int desiredScrollPos;
    public int scrollPos;
    public int scrollPosPrev;
    public int scrollPosInterp;

    public int recipetype;
    public ContainerRecipe slotcontainer;
    public GuiContainer firstGui;
    public GuiScreen prevGui;
    private GuiNEIButton nexttype;
    private GuiNEIButton prevtype;
    
    private int OVERLAY_BUTTON_ID_START = 4;
    private GuiButton[] overlayButtons;
    
    private final GuiScrollPaneViewport scroll = new GuiScrollPaneViewport(this);
    private IRecipeHandler handler;
    private HandlerInfo handlerInfo;
    
    private int yShift = 0;

    protected GuiRecipe(GuiScreen prevgui) {
        super(new ContainerRecipe());
        slotcontainer = (ContainerRecipe) inventorySlots;

        this.prevGui = prevgui;
        if(prevgui instanceof GuiContainer)
            this.firstGui = (GuiContainer)prevgui;

        if (prevgui instanceof IGuiContainerOverlay)
            this.firstGui = ((IGuiContainerOverlay) prevgui).getFirstScreen();
        
    }

   
    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        xSize = 176;
        ySize = Math.min(Math.max(height - 68, 166), 370);
        super.initGui();
        guiTop = (height - ySize) / 2 + 10;
        currenthandlers = getCurrentRecipeHandlers(); // Probably don't comment me out
        
        if(handler == null) 
            setRecipePage(recipetype);
        else
            initOverlayButtons();
        
        checkYShift();
        
        final int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
        final int leftButtonX = guiLeft + borderPadding;

        nexttype = new GuiNEIButton(0, leftButtonX, guiTop + 3, buttonWidth, buttonHeight, "<");
        nexttype.bypassScissor = true;
        prevtype = new GuiNEIButton(1, rightButtonX, guiTop + 3, buttonWidth, buttonHeight, ">");
        prevtype.bypassScissor = true;
        
        buttonList.addAll(Arrays.asList(nexttype, prevtype));

        if (currenthandlers.size() == 1) {
            nexttype.visible = false;
            prevtype.visible = false;
        }
        
        refreshPage();


    }

    @SuppressWarnings("unchecked")
    private void initOverlayButtons() {
        if (overlayButtons != null) {
            buttonList.removeIf(Arrays.asList(overlayButtons)::contains);
        }

        overlayButtons = new GuiButton[32];
        for (int i = 0 ; i < overlayButtons.length ; i++) {
            overlayButtons[i] = new GuiNEIButton(
                OVERLAY_BUTTON_ID_START + i,
                 0,
                 0,
                 buttonWidth,
                 buttonHeight,
                 "?", scroll
                );
        }
        Collections.addAll(buttonList, overlayButtons);

    }
    
    private void checkYShift() {
        yShift = handlerInfo == null ? 0 : handlerInfo.getYShift();
    }


    public void setRecipePage(int idx) {
        recipetype = idx;
        if (recipetype < 0) recipetype = currenthandlers.size() - 1;
        else if (recipetype >= currenthandlers.size()) recipetype = 0;

        handler = currenthandlers.get(recipetype);
        handlerInfo = getHandlerInfo(handler);
        scrollPos = 0;
        scrollPosInterp = 0;
        scrollPosPrev = 0;
        desiredScrollPos = 0;
        checkYShift();
        initOverlayButtons();
    }

    @Override
    protected boolean func_146978_c(int left, int top, int right, int bottom, int pointX, int pointY) {
        if (!scroll.isInViewportScreenSpace(pointX, pointY))
            return false;
        return super.func_146978_c(left, top, right, bottom, pointX, pointY);
    }

    @Override
    public void keyTyped(char c, int i) {
        if (i == Keyboard.KEY_ESCAPE) //esc
        {
            mc.displayGuiScreen(firstGui);
            return;
        }
        if (GuiContainerManager.getManager(this).lastKeyTyped(i, c))
            return;


        val si = getScrollInfo();
        for (int recipe = si.firstIndex; recipe < handler.numRecipes() && recipe < si.lastIndex; recipe++)
            if (handler.keyTyped(this, c, i, recipe))
                return;

        if (i == mc.gameSettings.keyBindInventory.getKeyCode())
            mc.displayGuiScreen(firstGui);
        else if (i == NEIClientConfig.getKeyBinding("gui.back"))
            mc.displayGuiScreen(prevGui);
        else if (i == NEIClientConfig.getKeyBinding("gui.prev_machine"))
            prevType();
        else if (i == NEIClientConfig.getKeyBinding("gui.next_machine"))
            nextType();

    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        val si = getScrollInfo();
        if (scroll.isInViewportScreenSpace(x, y)) {
            for (int recipe = si.firstIndex; recipe < handler.numRecipes() && recipe < si.lastIndex; recipe++)
                if (handler.mouseClicked(this, button, recipe))
                    return;
        }
        super.mouseClicked(x, y, button);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);
        switch (guibutton.id) {
            case 0:
                prevType();
                return;
            case 1:
                nextType();
                return;
        }
        if (overlayButtons != null && guibutton.id >= OVERLAY_BUTTON_ID_START && guibutton.id < OVERLAY_BUTTON_ID_START + overlayButtons.length) {
            val si = getScrollInfo();
            overlayRecipe(si.firstIndex + guibutton.id - OVERLAY_BUTTON_ID_START);
        }
    }

    @Override
    public void updateScreen() {
        scrollPosPrev = scrollPos;
        if (Math.abs(scrollPos - desiredScrollPos) < 2) {
            scrollPos = desiredScrollPos;
        }
        scrollPos = (scrollPos + desiredScrollPos) / 2;

        boolean clamped = false;
        if (scrollPos < 0) {
            scrollPos = 0;
            clamped = true;
        }
        val h = Math.max(0, getScrollContentHeight() - scroll.height + 30);
        if (scrollPos > h) {
            scrollPos = h;
            clamped = true;
        }
        if (clamped) {
            desiredScrollPos = scrollPos;
        }
        super.updateScreen();
        handler.onUpdate();
        refreshPage();
    }

    @Override
    public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
        if (!scroll.isInViewportScreenSpace(mousex, mousey))
            return currenttip;
        /*
         * Several mods try to figure out the relative mouse position for fluid tooltips.  This worked fine
         * when it was a static 166.. however now that we're scaling it this no longer works.  Rather than
         * patching the individual mods (so that we can be as compatible as possible), we'll just fake
         * the height for their tooltip rendering.
         */
        val si = getScrollInfo();
        // Begin Hax
        final int oldHeight = this.height;
        int handlerHeight = handler.overwriteHandlerInfoSettings() ? handler.height() : handlerInfo.getHeight();


        this.height = 166 + handlerHeight + 18 + (guiTop - 44) * 2;
        for (int i = si.firstIndex; i < handler.numRecipes() && i < si.lastIndex; i++) {
            currenttip = handler.handleTooltip(this, currenttip, i);
        }
        this.height = oldHeight;
        // End Hax
        
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack stack, int mousex, int mousey, List<String> currenttip) {
        if (!scroll.isInViewportScreenSpace(mousex, mousey))
            return currenttip;
        val si = getScrollInfo();
        for (int i = si.firstIndex; i < handler.numRecipes() && i < si.lastIndex; i++)
            currenttip = handler.handleItemTooltip(this, stack, currenttip, i);

        return currenttip;
    }

    @Override
    public List<String> handleItemDisplayName(GuiContainer gui, ItemStack itemstack, List<String> currenttip) {
        return currenttip;
    }

    private void nextType() {
        setRecipePage(++recipetype);
    }

    private void prevType() {
        setRecipePage(--recipetype);
    }


    public HandlerInfo getHandlerInfo(IRecipeHandler handler) {
        final String handlerName = handler.toString().split("@")[0];
        final String handlerID;
        if(handler instanceof TemplateRecipeHandler) {
            handlerID = (((TemplateRecipeHandler)handler).getOverlayIdentifier());
        } else {
            handlerID = null;
        }
        HandlerInfo info = HandlerInfoManager.getHandlerInfo(handlerName, handlerID);
        
        if (info == null)
            return HandlerInfoManager.DEFAULT_HANDLER_INFO;
        
        return info;
    }

    private void overlayRecipe(int recipe) {
        final IRecipeOverlayRenderer renderer = handler.getOverlayRenderer(firstGui, recipe);
        final IOverlayHandler overlayHandler = handler.getOverlayHandler(firstGui, recipe);
        final boolean shift = NEIClientUtils.shiftKey();

        if (handler != null && (renderer == null || shift)) {
            mc.displayGuiScreen(firstGui);
            overlayHandler.overlayRecipe(firstGui, currenthandlers.get(recipetype), recipe, shift);
        } else if (renderer != null) {
            mc.displayGuiScreen(firstGui);
            LayoutManager.overlayRenderer = renderer;
        }
    }
    
    public void refreshPage() {
        refreshSlots();

        scroll.x = 5;
        scroll.y = 2 + buttonHeight + 5;
        scroll.width = handlerInfo == null ? width - 10 : handlerInfo.getWidth();
        scroll.height = ySize - buttonHeight - 10;
        checkYShift();
        val si = getScrollInfo();

        val numRecipes = handler.numRecipes();
        if(firstGui == null) {
            for (GuiButton overlay : overlayButtons) {
                overlay.visible = false;
            }
        } else {
            for(int i = 0 ; i < overlayButtons.length ; i ++) {
                final int curRecipe = si.firstIndex + i;
                val button = overlayButtons[i];
                if (curRecipe >= numRecipes) {
                    button.visible = false;
                } else {
                    boolean visible = handler.hasOverlay(firstGui, firstGui.inventorySlots, curRecipe);
                    button.visible = visible;
                    if (visible) {
                        int x = guiLeft + scroll.x + scroll.width - 16;
                        int y = guiTop + scroll.y + (si.handlerHeight * (i + 1)) - 16 + si.firstOffset;
                        button.xPosition = x;
                        button.yPosition = y;
                    }
                }
            }
        }
    }

    private void refreshSlots() {
        slotcontainer.inventorySlots.clear();
        val si = getScrollInfo();
        for (int i = si.firstIndex; i < handler.numRecipes() && i < si.lastIndex; i++) {
            Point p = getRecipePosition(i);

            List<PositionedStack> stacks = handler.getIngredientStacks(i);
            for (PositionedStack stack : stacks)
                slotcontainer.addSlot(stack, p.x, p.y);

            stacks = handler.getOtherStacks(i);
            for (PositionedStack stack : stacks)
                slotcontainer.addSlot(stack, p.x, p.y);

            PositionedStack result = handler.getResultStack(i);
            if (result != null)
                slotcontainer.addSlot(result, p.x, p.y);
        }
    }

    @AllArgsConstructor
    private static class ScrollInfo {
        public int handlerHeight;
        public int firstIndex;
        public int firstOffset;
        public int lastIndex;
    }

    private ScrollInfo getScrollInfo() {
        int handlerHeight = getHandlerHeight();
        int firstIndex = scrollPosInterp / handlerHeight;
        firstIndex = Math.max(firstIndex, 0);
        int firstOffset = firstIndex * handlerHeight - scrollPosInterp;
        int lastIndex = (scrollPosInterp + scroll.height + handlerHeight - 1) / handlerHeight;
        return new ScrollInfo(handlerHeight, firstIndex, firstOffset, lastIndex);
    }

    private int getHandlerHeight() {
        if (handlerInfo == null) {
            return handler.height();
        } else {
            return handler.overwriteHandlerInfoSettings() ? handler.height() : handlerInfo.getHeight();
        }
    }

    private int getScrollContentHeight() {
        return getHandlerHeight() * handler.numRecipes();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        scrollPosInterp = (int) MathHelper.denormalizeClamp(scrollPosPrev, scrollPos, partialTicks) - 15;
        refreshPage();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GuiContainerManager.enable2DRender();

        val si = getScrollInfo();
        GL11.glPushMatrix();
        GL11.glTranslatef(scroll.x, scroll.y + yShift + si.firstOffset, 0);
        for (int i = si.firstIndex; i < handler.numRecipes() && i < si.lastIndex; i++) {
            handler.drawForeground(i);
            GL11.glTranslatef(0, si.handlerHeight, 0);
        }
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        String s = handler.getRecipeName().trim();
        fontRendererObj.drawStringWithShadow(s, (xSize - fontRendererObj.getStringWidth(s)) / 2, 5, 0xffffff);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor3f(1, 1, 1);
        val tess = Tessellator.instance;
        tess.startDrawingQuads();
        float color = 0.1882353f;
        tess.setColorRGBA_F(color, color, color, 1);
        tess.addVertex(scroll.x + scroll.width, scroll.y, 0);
        tess.addVertex(scroll.x, scroll.y, 0);
        tess.setColorRGBA_F(color, color, color, 0);
        tess.addVertex(scroll.x, scroll.y + 15, 0);
        tess.addVertex(scroll.x + scroll.width, scroll.y + 15, 0);
        tess.setColorRGBA_F(color, color, color, 1);
        tess.addVertex(scroll.x, scroll.y + scroll.height, 0);
        tess.addVertex(scroll.x + scroll.width, scroll.y + scroll.height, 0);
        tess.setColorRGBA_F(color, color, color, 0);
        tess.addVertex(scroll.x + scroll.width, scroll.y + scroll.height - 15, 0);
        tess.addVertex(scroll.x, scroll.y + scroll.height - 15, 0);
        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        final int j = (width  - xSize) / 2;
        final int k = guiTop;
        
        drawBackgroundTiled(j, k);

        drawRect(guiLeft + borderPadding + buttonWidth - 1,
                 nexttype.yPosition,
                 guiLeft + xSize - borderPadding - buttonWidth,
                 nexttype.yPosition + buttonHeight,
                 0x30000000);

        val si = getScrollInfo();
        val mc = Minecraft.getMinecraft();
        val sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        double guiScale = mc.displayWidth / sr.getScaledWidth_double();
        int viewportLeft = guiLeft + scroll.x;
        int viewportTop = guiTop + scroll.y;
        int viewportRight = viewportLeft + scroll.width;
        int viewportBottom = viewportTop + scroll.height;
        viewportLeft = (int)(viewportLeft * guiScale);
        viewportTop = (int)(viewportTop * guiScale);
        viewportRight = (int)(viewportRight * guiScale);
        viewportBottom = (int)(viewportBottom * guiScale);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(viewportLeft, mc.displayHeight - viewportBottom, viewportRight - viewportLeft, viewportBottom - viewportTop);
        GL11.glPushMatrix();
        GL11.glTranslatef(j + scroll.x, k + scroll.y + yShift + si.firstOffset, 0);
        for (int i = si.firstIndex; i < handler.numRecipes() && i < si.lastIndex; i++) {
            handler.drawBackground(i);
            GL11.glTranslatef(0, si.handlerHeight, 0);
        }
        GL11.glPopMatrix();
    }

    private void drawBackgroundTiled(int j, int k) {
        // Draw the top
        bgTop.draw(j, k + BG_TOP_Y);

        // Maybe Tile the middle
        final int tiledHeight = ySize - BG_TOP_HEIGHT - BG_BOTTOM_HEIGHT;

        if (tiledHeight > 0) {
            final int yTileCount = tiledHeight / BG_MIDDLE_HEIGHT;
            final int yRemainder = tiledHeight - (yTileCount * BG_MIDDLE_HEIGHT);
            final int yStart = k + BG_TOP_HEIGHT + tiledHeight;
            
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                final int tileHeight = (yTile == yTileCount) ? yRemainder : BG_MIDDLE_HEIGHT;
                final int y = yStart - ((yTile + 1) * BG_MIDDLE_HEIGHT);
                if (tileHeight > 0) {
                    bgMiddle.draw(j, y, BG_MIDDLE_HEIGHT - tileHeight, 0, 0, 0);
                }
            }
        }

        // Draw the bottom
        bgBottom.draw(j, k + ySize - BG_BOTTOM_HEIGHT);
    }

    @Override
    public GuiContainer getFirstScreen() {
        return firstGui;
    }

    public boolean isMouseOver(PositionedStack stack, int recipe) {
        Slot stackSlot = slotcontainer.getSlotWithStack(stack, getRecipePosition(recipe).x, getRecipePosition(recipe).y);
        Point mousepos = GuiDraw.getMousePosition();
        Slot mouseoverSlot = getSlotAtPosition(mousepos.x, mousepos.y);

        return stackSlot == mouseoverSlot;
    }

    public Point getRecipePosition(int recipe) {
        int handlerHeight = getHandlerHeight();
        int offsetInScrollPane = recipe * handlerHeight;
        int offsetInViewport = offsetInScrollPane - scrollPosInterp;
        return new Point(scroll.x, scroll.y + yShift + offsetInViewport);
    }

    @Override
    public void mouseScrolled(int i) {
        if (new Rectangle(guiLeft, guiTop, xSize, ySize).contains(GuiDraw.getMousePosition())) {
            desiredScrollPos -= i * 10;
        }
    }

    public abstract ArrayList<? extends IRecipeHandler> getCurrentRecipeHandlers();

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility)
    {
        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return Collections.emptyList();
    }


    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        // Because some of the handlers *cough avaritia* are oversized
        Rectangle rect = new Rectangle(scroll.x, scroll.y, scroll.width, scroll.height);

        return rect.contains(x, y, w, h);
    }

}
