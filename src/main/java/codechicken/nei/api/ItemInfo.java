package codechicken.nei.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.collect.ArrayListMultimap;

import codechicken.core.featurehack.GameDataManipulator;
import codechicken.nei.InfiniteStackSizeHandler;
import codechicken.nei.InfiniteToolHandler;
import codechicken.nei.ItemList.AnyMultiItemFilter;
import codechicken.nei.ItemList.PatternItemFilter;
import codechicken.nei.ItemMobSpawner;
import codechicken.nei.ItemStackMap;
import codechicken.nei.ItemStackSet;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PopupInputHandler;
import codechicken.nei.PresetsList;
import codechicken.nei.SearchField.SearchParserProvider;
import codechicken.nei.SearchTokenParser.SearchMode;
import codechicken.nei.config.ArrayDumper;
import codechicken.nei.config.HandlerDumper;
import codechicken.nei.config.ItemPanelDumper;
import codechicken.nei.config.RegistryDumper;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.BrewingRecipeHandler;
import codechicken.nei.recipe.RecipeItemInputHandler;
import codechicken.nei.search.IdentifierFilter;
import codechicken.nei.search.ModNameFilter;
import codechicken.nei.search.OreDictionaryFilter;
import codechicken.nei.search.TooltipFilter;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

/**
 * This is an internal class for storing information about items, to be accessed by the API
 */
public class ItemInfo {

    public static enum Layout {
        HEADER,
        BODY,
        FOOTER
    }

    public static final ArrayListMultimap<Layout, IHighlightHandler> highlightHandlers = ArrayListMultimap.create();
    public static final ItemStackMap<String> nameOverrides = new ItemStackMap<>();
    public static final ItemStackSet hiddenItems = new ItemStackSet();
    public static final AnyMultiItemFilter hiddenItemsRules = new AnyMultiItemFilter();
    public static final ItemStackSet finiteItems = new ItemStackSet();
    public static final ArrayListMultimap<Item, ItemStack> itemOverrides = ArrayListMultimap.create();
    public static final ArrayListMultimap<Item, ItemStack> itemVariants = ArrayListMultimap.create();

    public static final LinkedList<IInfiniteItemHandler> infiniteHandlers = new LinkedList<>();
    public static final ArrayListMultimap<Block, IHighlightHandler> highlightIdentifiers = ArrayListMultimap.create();
    public static final HashSet<Class<? extends Slot>> fastTransferExemptions = new HashSet<>();

    public static final HashMap<Item, String> itemOwners = new HashMap<>();

    public static boolean isHidden(ItemStack stack) {
        return hiddenItems.contains(stack) || hiddenItemsRules.matches(stack);
    }

    @Deprecated
    public static boolean isHidden(Item item) {
        return hiddenItems.containsAll(item);
    }

    public static String getNameOverride(ItemStack stack) {
        return nameOverrides.get(stack);
    }

    public static boolean canBeInfinite(ItemStack stack) {
        return !finiteItems.contains(stack);
    }

    /**
     * @deprecated Use field directly
     */
    @Deprecated
    public static List<ItemStack> getItemOverrides(Item item) {
        return itemOverrides.get(item);
    }

    public static void preInit() {
        addMobSpawnerItem();
    }

    public static void load() {
        addVanillaBlockProperties();
        addDefaultDropDowns();
        searchItems();
        parseModItems();
        ItemMobSpawner.loadSpawners();
        addSpawnEggs();
        addInfiniteHandlers();
        addInputHandlers();
        addIDDumps();
        addSearchProviders();
        RecipeItemInputHandler.load();
        PresetsList.load();
    }

    private static void addSearchProviders() {
        API.addSearchProvider(
                new SearchParserProvider('\0', "default", EnumChatFormatting.RESET, PatternItemFilter::new) {

                    @Override
                    public SearchMode getSearchMode() {
                        return SearchMode.ALWAYS;
                    }

                });
        API.addSearchProvider(
                new SearchParserProvider('@', "modName", EnumChatFormatting.LIGHT_PURPLE, ModNameFilter::new));
        API.addSearchProvider(
                new SearchParserProvider('$', "oreDict", EnumChatFormatting.AQUA, OreDictionaryFilter::new));
        API.addSearchProvider(new SearchParserProvider('#', "tooltip", EnumChatFormatting.YELLOW, TooltipFilter::new));
        API.addSearchProvider(
                new SearchParserProvider('&', "identifier", EnumChatFormatting.GOLD, IdentifierFilter::new));
    }

    private static void addIDDumps() {
        API.addOption(new RegistryDumper<Item>("tools.dump.item") {

            @Override
            public String[] header() {
                return new String[] { "Name", "ID", "Has Block", "Mod", "Class", "Display Name" };
            }

            @Override
            public String[] dump(Item item, int id, String name) {
                return new String[] { name, Integer.toString(id),
                        Boolean.toString(Block.getBlockFromItem(item) != Blocks.air), ItemInfo.itemOwners.get(item),
                        item.getClass().getCanonicalName(), EnumChatFormatting.getTextWithoutFormattingCodes(
                                GuiContainerManager.itemDisplayNameShort(new ItemStack(item))) };
            }

            @Override
            public RegistryNamespaced registry() {
                return Item.itemRegistry;
            }
        });
        API.addOption(new RegistryDumper<Block>("tools.dump.block") {

            @Override
            public String[] header() {
                return new String[] { "Name", "ID", "Has Item", "Mod", "Class", "Display Name" };
            }

            @Override
            public String[] dump(Block block, int id, String name) {
                final Item item = Item.getItemFromBlock(block);
                return new String[] { name, Integer.toString(id), Boolean.toString(item != null),
                        ItemInfo.itemOwners.get(item), block.getClass().getCanonicalName(),
                        item != null ? EnumChatFormatting.getTextWithoutFormattingCodes(
                                GuiContainerManager.itemDisplayNameShort(new ItemStack(item))) : "null" };
            }

            @Override
            public RegistryNamespaced registry() {
                return Block.blockRegistry;
            }
        });
        API.addOption(new ArrayDumper<Potion>("tools.dump.potion") {

            public String[] header() {
                return new String[] { "ID", "Unlocalised name", "Class" };
            }

            @Override
            public String[] dump(Potion potion, int id) {
                return new String[] { Integer.toString(id), potion.getName(), potion.getClass().getCanonicalName() };
            }

            @Override
            public Potion[] array() {
                return Potion.potionTypes;
            }
        });
        API.addOption(new ArrayDumper<Enchantment>("tools.dump.enchantment") {

            public String[] header() {
                return new String[] { "ID", "Unlocalised name", "Type", "Min Level", "Max Level", "Class" };
            }

            @Override
            public String[] dump(Enchantment ench, int id) {
                return new String[] { Integer.toString(id), ench.getName(), ench.type.toString(),
                        Integer.toString(ench.getMinLevel()), Integer.toString(ench.getMaxLevel()),
                        ench.getClass().getCanonicalName() };
            }

            @Override
            public Enchantment[] array() {
                return Enchantment.enchantmentsList;
            }
        });
        API.addOption(new ArrayDumper<BiomeGenBase>("tools.dump.biome") {

            @Override
            public String[] header() {
                return new String[] { "ID", "Name", "Temperature", "Rainfall", "Spawn Chance", "Root Height",
                        "Height Variation", "Types", "Class" };
            }

            @Override
            public String[] dump(BiomeGenBase biome, int id) {
                BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(biome);
                StringBuilder s_types = new StringBuilder();
                for (BiomeDictionary.Type t : types) {
                    if (s_types.length() > 0) s_types.append(", ");
                    s_types.append(t.name());
                }

                return new String[] { Integer.toString(id), biome.biomeName,
                        Float.toString(biome.getFloatTemperature(0, 0, 0)), Float.toString(biome.getFloatRainfall()),
                        Float.toString(biome.getSpawningChance()), Float.toString(biome.rootHeight),
                        Float.toString(biome.heightVariation), s_types.toString(),
                        biome.getClass().getCanonicalName() };
            }

            @Override
            public BiomeGenBase[] array() {
                return BiomeGenBase.getBiomeGenArray();
            }
        });
        API.addOption(new ItemPanelDumper("tools.dump.itempanel"));
        API.addOption(new HandlerDumper("tools.dump.handlers"));
    }

    @SuppressWarnings("unchecked")
    private static void parseModItems() {
        final Map<String, ItemStackSet> modSubsets = new HashMap<>();

        for (Item item : (Iterable<Item>) Item.itemRegistry) {
            UniqueIdentifier ident = null;
            try {
                ident = GameRegistry.findUniqueIdentifierFor(item);
            } catch (Exception ignored) {}

            if (ident == null) {
                NEIClientConfig.logger.error("Failed to find identifier for: " + item);
                continue;
            }

            itemOwners.put(item, ident.modId);
            modSubsets.computeIfAbsent(ident.modId, i -> new ItemStackSet()).with(item);
        }

        API.addSubset("Mod.Minecraft", modSubsets.remove("minecraft"));

        for (Entry<String, ItemStackSet> entry : modSubsets.entrySet()) {
            ModContainer mc = FMLCommonHandler.instance().findContainerFor(entry.getKey());
            if (mc == null) NEIClientConfig.logger.error("Missing container for " + entry.getKey());
            else API.addSubset("Mod." + mc.getName(), entry.getValue());
        }
    }

    private static void addInputHandlers() {
        GuiContainerManager.addInputHandler(new PopupInputHandler());
    }

    private static void addMobSpawnerItem() {
        GameDataManipulator.replaceItem(Block.getIdFromBlock(Blocks.mob_spawner), new ItemMobSpawner());
    }

    private static void addInfiniteHandlers() {
        API.addInfiniteItemHandler(new InfiniteStackSizeHandler());
        API.addInfiniteItemHandler(new InfiniteToolHandler());
    }

    private static void addVanillaBlockProperties() {
        API.setOverrideName(new ItemStack(Blocks.flowing_water), "Water Source");
        API.setOverrideName(new ItemStack(Blocks.water), "Water Still");
        API.setOverrideName(new ItemStack(Blocks.flowing_lava), "Lava Source");
        API.setOverrideName(new ItemStack(Blocks.lava), "Lava Still");
        API.setOverrideName(new ItemStack(Blocks.end_portal), "End Portal");
        API.setOverrideName(new ItemStack(Blocks.end_portal_frame), "End Portal Frame");
        API.hideItem(new ItemStack(Blocks.double_stone_slab, 1, OreDictionary.WILDCARD_VALUE));
        API.hideItem(new ItemStack(Blocks.double_wooden_slab, 1, OreDictionary.WILDCARD_VALUE));
        API.hideItem(new ItemStack(Blocks.carrots));
        API.hideItem(new ItemStack(Blocks.potatoes));
        API.hideItem(new ItemStack(Blocks.cocoa));
    }

    private static void addDefaultDropDowns() {
        final Item mob_spawner = Item.getItemFromBlock(Blocks.mob_spawner);
        API.addSubset("Items", item -> Block.getBlockFromItem(item.getItem()) == Blocks.air);
        API.addSubset("Blocks", item -> Block.getBlockFromItem(item.getItem()) != Blocks.air);
        API.addSubset("Blocks.MobSpawners", item -> item.getItem() == mob_spawner);
    }

    @SuppressWarnings("unchecked")
    private static void searchItems() {
        final Map<Integer, ItemStackSet> creativeTabRanges = new HashMap<>();
        final List<ItemStack> stackList = new LinkedList<>();

        for (Item item : (Iterable<Item>) Item.itemRegistry) {
            if (item == null) continue;

            for (CreativeTabs itemTab : item.getCreativeTabs()) {
                if (itemTab != null) {
                    final ItemStackSet itemset = creativeTabRanges
                            .computeIfAbsent(itemTab.getTabIndex(), i -> new ItemStackSet());

                    try {
                        stackList.clear();
                        item.getSubItems(item, itemTab, stackList);
                        itemset.addAll(stackList);
                    } catch (Exception e) {
                        NEIClientConfig.logger
                                .error("Error loading sub-items for: " + item + ". Tab: " + itemTab.getTabLabel(), e);
                    }
                }
            }

            try {
                final List<ItemStack> subItems = new LinkedList<>();
                item.getSubItems(item, null, subItems);

                for (ItemStack stack : subItems) {
                    if (item.isPotionIngredient(stack) && item.getPotionEffect(stack) != null) {
                        BrewingRecipeHandler.ingredients.add(stack);
                    }
                }

            } catch (Exception e) {
                NEIClientConfig.logger.error("Error loading brewing ingredients for: " + item, e);
            }
        }

        API.addSubset("Items.Tools.Pickaxes", item -> item.getItem() instanceof ItemPickaxe);
        API.addSubset("Items.Tools.Shovels", item -> item.getItem() instanceof ItemSpade);
        API.addSubset("Items.Tools.Axes", item -> item.getItem() instanceof ItemAxe);
        API.addSubset("Items.Tools.Hoes", item -> item.getItem() instanceof ItemHoe);
        API.addSubset(
                "Items.Tools.Other",
                item -> item.getItem() == Items.fishing_rod || item.getItem() == Items.flint_and_steel
                        || item.getItem() == Items.shears);
        API.addSubset("Items.Weapons.Swords", item -> item.getItem() instanceof ItemSword);
        API.addSubset("Items.Weapons.Ranged", item -> item.getItem() == Items.arrow || item.getItem() == Items.bow);
        API.addSubset("Items.Armor.Helmets", item -> item.getItem() instanceof ItemArmor armor && armor.armorType == 0);
        API.addSubset(
                "Items.Armor.Chestplates",
                item -> item.getItem() instanceof ItemArmor armor && armor.armorType == 1);
        API.addSubset(
                "Items.Armor.Leggings",
                item -> item.getItem() instanceof ItemArmor armor && armor.armorType == 2);
        API.addSubset("Items.Armor.Boots", item -> item.getItem() instanceof ItemArmor armor && armor.armorType == 3);
        API.addSubset("Items.Food", item -> item.getItem() instanceof ItemFood);
        API.addSubset("Items.Potions.Ingredients", BrewingRecipeHandler.ingredients);

        for (CreativeTabs tab : CreativeTabs.creativeTabArray) {
            final ItemStackSet itemset = creativeTabRanges.get(tab.getTabIndex());
            if (itemset != null && !itemset.isEmpty()) {
                API.addSubset("CreativeTabs." + I18n.format(tab.getTranslatedTabLabel()), itemset);
            }
        }

        BrewingRecipeHandler.searchPotions();
    }

    private static void addSpawnEggs() {
        addEntityEgg(EntitySnowman.class, 0xEEFFFF, 0xffa221);
        addEntityEgg(EntityIronGolem.class, 0xC5C2C1, 0xffe1cc);
    }

    private static void addEntityEgg(Class<?> entity, int i, int j) {
        int id = (Integer) EntityList.classToIDMapping.get(entity);
        EntityList.entityEggs.put(id, new EntityEggInfo(id, i, j));
    }

    public static ArrayList<ItemStack> getIdentifierItems(World world, EntityPlayer player, MovingObjectPosition hit) {
        int x = hit.blockX;
        int y = hit.blockY;
        int z = hit.blockZ;
        Block mouseoverBlock = world.getBlock(x, y, z);

        ArrayList<ItemStack> items = new ArrayList<>();

        ArrayList<IHighlightHandler> handlers = new ArrayList<>();
        if (highlightIdentifiers.containsKey(null)) handlers.addAll(highlightIdentifiers.get(null));
        if (highlightIdentifiers.containsKey(mouseoverBlock)) handlers.addAll(highlightIdentifiers.get(mouseoverBlock));
        for (IHighlightHandler ident : handlers) {
            ItemStack item = ident.identifyHighlight(world, player, hit);
            if (item != null) items.add(item);
        }

        if (items.size() > 0) return items;

        ItemStack pick = mouseoverBlock.getPickBlock(hit, world, x, y, z, player);
        if (pick != null) items.add(pick);

        try {
            items.addAll(mouseoverBlock.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0));
        } catch (Exception ignored) {}
        if (mouseoverBlock instanceof IShearable) {
            IShearable shearable = (IShearable) mouseoverBlock;
            if (shearable.isShearable(new ItemStack(Items.shears), world, x, y, z))
                items.addAll(shearable.onSheared(new ItemStack(Items.shears), world, x, y, z, 0));
        }

        if (items.size() == 0) items.add(0, new ItemStack(mouseoverBlock, 1, world.getBlockMetadata(x, y, z)));

        return items;
    }

    public static void registerHighlightHandler(IHighlightHandler handler, ItemInfo.Layout... layouts) {
        for (ItemInfo.Layout layout : layouts) ItemInfo.highlightHandlers.get(layout).add(handler);
    }

    public static List<String> getText(ItemStack itemStack, World world, EntityPlayer player,
            MovingObjectPosition mop) {
        List<String> retString = new ArrayList<>();

        for (ItemInfo.Layout layout : ItemInfo.Layout.values())
            for (IHighlightHandler handler : ItemInfo.highlightHandlers.get(layout))
                retString = handler.handleTextData(itemStack, world, player, mop, retString, layout);

        return retString;
    }

    @Deprecated
    public static String getSearchName(ItemStack stack) {
        return GuiContainerManager.concatenatedDisplayName(stack, true).toLowerCase();
    }
}
