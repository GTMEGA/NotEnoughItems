package codechicken.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * TODO:
 *   1) Bookmark button to toggle visibility
 */
public class BookmarkPanel extends ItemPanel {

    public void init() {
        super.init();
    }

    public String getLabelText() {
        return super.getLabelText() + " [" + items.size() + "]";
    }

    public void addOrRemoveItem(ItemStack item) {
        ItemStack normalized = normalize(item);
        if(!remove(normalized)) {
            _items.add(normalized);
        }
        saveBookmarks();
    }

    public void saveBookmarks() {
        List<String> strings = new ArrayList<>();
        for (ItemStack item:_items) {
            strings.add(item.writeToNBT(new NBTTagCompound()).toString());
        }
        File file = NEIClientConfig.bookmarkFile;
        if(file != null) {
            try(FileWriter writer = new FileWriter(file)) {
                IOUtils.writeLines(strings, "\n", writer);
            } catch (IOException e) {
                NEIClientConfig.logger.error("Filed to save bookmarks list to file {}", file, e);
            }
        }
    }

    public void loadBookmarks() {
        File file = NEIClientConfig.bookmarkFile;
        if (file == null || !file.exists()) {
            return;
        }
        List<String> itemStrings;
        try (FileReader reader = new FileReader(file)) {
            NEIClientConfig.logger.info("Loading bookmarks from file {}", file);
            itemStrings = IOUtils.readLines(reader);
        } catch (IOException e) {
            NEIClientConfig.logger.error("Failed to load bookmarks from file {}", file, e);
            return;
        }
        _items.clear();
        for (String itemStr: itemStrings) {
            try {
                NBTTagCompound itemStackNBT = (NBTTagCompound)JsonToNBT.func_150315_a(itemStr);
                ItemStack itemStack = ItemStack.loadItemStackFromNBT(itemStackNBT);
                if (itemStack != null) {
                    _items.add(itemStack);
                } else {
                    NEIClientConfig.logger.warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStr);
                }
            } catch (NBTException e) {
                NEIClientConfig.logger.error("Failed to load bookmarked ItemStack from json string:\n{}", itemStr, e);
            }
        }
    }

    protected ItemStack normalize(ItemStack item) {
        ItemStack copy = item.copy();
        copy.stackSize = 1;
        return copy;
    }
    private boolean remove(ItemStack item) {
        int i = 0;
        for (ItemStack existing : _items) {
            if (existing == item || existing.isItemEqual(item)) {
                _items.remove(i);
                return true;
            }
            i++;
        }
        return false;
    }


    @Override
    public int getMarginLeft() {
        return 5;
    }


    @Override
    public int getButtonTop() {
        LayoutStyleMinecraft layout = (LayoutStyleMinecraft)LayoutManager.getLayoutStyle();
        return 2 + (((int)Math.ceil((double)layout.buttonCount / layout.numButtons)) * 18);
    }

    @Override
    public int getNextX(GuiContainer gui) {
        return gui.width - prev.w - 2 - ((gui.xSize + gui.width) /2 + 2);
    }

    @Override
    public int getPrevX(GuiContainer gui) {
        return 2;
    }

    @Override
    public int getPageX(GuiContainer gui) {
        return gui.guiLeft * 3 / 2 + gui.xSize + 1 - ((gui.xSize + gui.width) /2 + 2);
    }

    public int getHightAdjustment() {
        return 0;
    }

    public int getWidth(GuiContainer gui) {
        return LayoutManager.getLeftSize(gui) - x;

    }

}
