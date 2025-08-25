package codechicken.nei.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.drawable.DrawableBuilder;
import codechicken.nei.drawable.DrawableResource;

public class HandlerInfo {

    public static int DEFAULT_HEIGHT = 65;
    public static int DEFAULT_WIDTH = 166;
    public static int DEFAULT_MAX_PER_PAGE = 1;

    private String handlerName;

    private String modName;
    private String modId;
    private boolean requiresMod;
    private String excludedModId;

    private String itemId;
    private String nbtString;

    private int yShift = 0;
    private int height = DEFAULT_HEIGHT;
    private int width = DEFAULT_WIDTH;
    private int maxRecipesPerPage = DEFAULT_MAX_PER_PAGE;
    private boolean showFavoritesButton = true;
    private boolean showOverlayButton = true;

    private ItemStack itemStack = null;
    private DrawableResource image = null;

    public HandlerInfo(String handlerName, String modName, String modId, boolean requiresMod, String excludedModId) {
        this.handlerName = handlerName;
        this.modName = modName;
        this.modId = modId;
        this.requiresMod = requiresMod;
        this.excludedModId = excludedModId;
    }

    public void setHandlerDimensions(int height, int width, int maxRecipesPerPage) {
        this.height = height;
        this.width = width;
        this.maxRecipesPerPage = maxRecipesPerPage;
    }

    public boolean setItem(String itemId, String nbtString) {
        if (hasImageOrItem()) return false;

        itemStack = NEIServerUtils.getModdedItem(itemId, nbtString);
        if (itemStack == null) NEIClientConfig.logger.info("Couldn't find " + modName + " - " + itemId);
        else {
            this.itemId = itemId;
            this.nbtString = nbtString;
        }
        return (itemStack != null);
    }

    public boolean setImage(String resourceLocation, int imageX, int imageY, int imageWidth, int imageHeight) {
        if (hasImageOrItem()) return false;

        this.image = new DrawableBuilder(resourceLocation, imageX, imageY, imageWidth, imageHeight).build();
        return true;
    }

    public DrawableResource getImage() {
        return image;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getModName() {
        return modName;
    }

    public String getModId() {
        return modId;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getMaxRecipesPerPage() {
        return Math.max(maxRecipesPerPage, 1);
    }

    public int getYShift() {
        return yShift;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public boolean hasImageOrItem() {
        if (image != null) return true;
        if (itemStack != null) return true;

        return false;
    }

    public void setYShift(int yShift) {
        this.yShift = yShift;
    }

    public boolean getShowFavoritesButton() {
        return this.showFavoritesButton;
    }

    public void setShowFavoritesButton(boolean showFavoritesButton) {
        this.showFavoritesButton = showFavoritesButton;
    }

    public boolean getShowOverlayButton() {
        return this.showOverlayButton;
    }

    public void setShowOverlayButton(boolean showOverlayButton) {
        this.showOverlayButton = showOverlayButton;
    }

    public static class Builder {

        private final HandlerInfo info;

        public Builder(String handlerName, String modName, String modId) {
            this.info = new HandlerInfo(handlerName, modName, modId, true, null);
            setMaxRecipesPerPage(Integer.MAX_VALUE);
        }

        public Builder(Class<? extends IRecipeHandler> handlerClazz, String modName, String modId) {
            this(handlerClazz.getName(), modName, modId);
        }

        public Builder setDisplayStack(ItemStack stack) {
            info.image = null;
            info.itemStack = stack;
            return this;
        }

        public Builder setDisplayImage(DrawableResource drawable) {
            info.itemStack = null;
            info.image = drawable;
            return this;
        }

        public Builder setDisplayImage(ResourceLocation location, int imageX, int imageY, int imageWidth,
                int imageHeight) {
            info.itemStack = null;
            info.setImage(location.toString(), imageX, imageY, imageWidth, imageHeight);
            return this;
        }

        public Builder setShiftY(int shiftY) {
            info.setYShift(shiftY);
            return this;
        }

        public Builder setWidth(int width) {
            info.setHandlerDimensions(info.height, width, info.maxRecipesPerPage);
            return this;
        }

        public Builder setHeight(int height) {
            info.setHandlerDimensions(height, info.width, info.maxRecipesPerPage);
            return this;
        }

        public Builder setMaxRecipesPerPage(int maxRecipesPerPage) {
            info.setHandlerDimensions(info.height, info.width, maxRecipesPerPage);
            return this;
        }

        public HandlerInfo build() {
            return info;
        }
    }
}
