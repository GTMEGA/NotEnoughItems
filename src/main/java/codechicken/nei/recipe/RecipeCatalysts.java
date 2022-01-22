package codechicken.nei.recipe;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeCatalysts {
    private static final Map<Class<? extends IRecipeHandler>, List<ItemStack>> recipeCatalystMap = new HashMap<>();
    private static Map<Class<? extends IRecipeHandler>, List<PositionedStack>> positionedRecipeCatalystMap = new HashMap<>();
    private static int heightCache;

    public static void addRecipeCatalyst(List<ItemStack> stacks, Class<? extends IRecipeHandler> handler) {
        if (recipeCatalystMap.containsKey(handler)) {
            recipeCatalystMap.get(handler).addAll(stacks);
        } else {
            recipeCatalystMap.put(handler, new ArrayList<>(stacks));
        }
    }

    public static Map<Class<? extends IRecipeHandler>, List<PositionedStack>> getPositionedRecipeCatalystMap() {
        return positionedRecipeCatalystMap;
    }

    public static List<PositionedStack> getRecipeCatalysts(Class<? extends IRecipeHandler> handler) {
        if (!NEIClientConfig.areJEIStyleTabsVisible() || !NEIClientConfig.areJEIStyleRecipeCatalystsVisible()) {
            return Collections.emptyList();
        }
        return positionedRecipeCatalystMap.getOrDefault(handler, Collections.emptyList());
    }

    public static boolean containsCatalyst(Class<? extends IRecipeHandler> handler, ItemStack candidate) {
        return recipeCatalystMap.getOrDefault(handler, Collections.emptyList()).stream()
            .anyMatch(s -> NEIServerUtils.areStacksSameType(s, candidate));
    }

    public static void updatePosition(int availableHeight) {
        if (availableHeight == heightCache) return;

        Map<Class<? extends IRecipeHandler>, List<PositionedStack>> newMap = new HashMap<>();
        for (Map.Entry<Class<? extends IRecipeHandler>, List<ItemStack>> entry : recipeCatalystMap.entrySet()) {
            List<ItemStack> catalysts = entry.getValue();
            List<PositionedStack> newStacks = new ArrayList<>();
            int rowCount = getRowCount(availableHeight, catalysts.size());

            for (int index = 0; index < catalysts.size(); index++) {
                ItemStack catalyst = catalysts.get(index);
                int column = index / rowCount;
                int row = index % rowCount;
                newStacks.add(new PositionedStack(catalyst, -column * GuiRecipeCatalyst.ingredientSize, row * GuiRecipeCatalyst.ingredientSize));
            }
            newMap.put(entry.getKey(), newStacks);
        }
        positionedRecipeCatalystMap = newMap;
        heightCache = availableHeight;
    }

    public static int getHeight() {
        return heightCache;
    }

    public static int getColumnCount(int availableHeight, int catalystsSize) {
        int maxItemsPerColumn = availableHeight / GuiRecipeCatalyst.ingredientSize;
        return NEIServerUtils.divideCeil(catalystsSize, maxItemsPerColumn);
    }

    public static int getRowCount(int availableHeight, int catalystsSize) {
        int columnCount = getColumnCount(availableHeight, catalystsSize);
        return NEIServerUtils.divideCeil(catalystsSize, columnCount);
    }

}
