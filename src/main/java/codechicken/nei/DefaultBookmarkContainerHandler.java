package codechicken.nei;

import codechicken.nei.api.IBookmarkContainerHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;

public class DefaultBookmarkContainerHandler implements IBookmarkContainerHandler {

    @Override
    public void pullBookmarkItemsFromContainer(GuiContainer guiContainer, ArrayList<ItemStack> bookmarkItems) {
        FastTransferManager manager = new FastTransferManager();
        LinkedList<ItemStack> containerStacks = manager.saveContainer(guiContainer.inventorySlots);

        for (ItemStack bookmarkItem : bookmarkItems) {

            int bookmarkSizeBackup = bookmarkItem.stackSize;

            for (int i = 0; i < containerStacks.size() - 4 * 9; i++) { // Last 36 slots are player inventory
                ItemStack containerItem = containerStacks.get(i);

                if (containerItem == null) {
                    continue;
                }

                if (bookmarkItem.isItemEqual(containerItem)) {
                    if (bookmarkItem.stackSize <= 0) {
                        break;
                    }

                    int transferAmount = Math.min(bookmarkItem.stackSize, containerItem.stackSize);

                    manager.transferItems(guiContainer, i, transferAmount);
                    bookmarkItem.stackSize -= transferAmount;

                    if (bookmarkItem.stackSize == 0) {
                        break;
                    }
                }
            }
            bookmarkItem.stackSize = bookmarkSizeBackup;
        }
    }
}
