package ninjaphenix.expandedstorage;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import ninjaphenix.expandedstorage.item.StorageConversionKit;
import ninjaphenix.expandedstorage.item.StorageMutator;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

import java.util.function.Consumer;

public final class BaseCommon {
    public static final ItemGroup GROUP = PlatformUtils.getInstance().createTab(() -> new ItemStack(Registry.ITEM.get(Utils.id("netherite_chest"))));

    record ItemRegistryEntry(Identifier id, Item object) {

    }

    static void initialize(Consumer<ItemRegistryEntry[]> itemRegistration) {
        ItemRegistryEntry[] items = new ItemRegistryEntry[1 + factorial(6)];
        items[0] = new ItemRegistryEntry(Utils.id("chest_mutator"), new StorageMutator(new Item.Settings().maxCount(1).group(BaseCommon.GROUP)));
        BaseCommon.defineTierUpgradePath(items, Utils.WOOD_TIER, Utils.IRON_TIER, Utils.GOLD_TIER, Utils.DIAMOND_TIER, Utils.OBSIDIAN_TIER, Utils.NETHERITE_TIER);
        itemRegistration.accept(items);
    }

    public static Identifier registerStat(Identifier stat) {
        Identifier rv = Registry.register(Registry.CUSTOM_STAT, stat, stat); // Forge doesn't provide registries for stats
        Stats.CUSTOM.getOrCreateStat(rv);
        return rv;
    }

    private static void defineTierUpgradePath(ItemRegistryEntry[] items, Tier... tiers) {
        int numTiers = tiers.length;
        int index = 1;
        for (int fromIndex = 0; fromIndex < numTiers - 1; fromIndex++) {
            Tier fromTier = tiers[fromIndex];
            for (int toIndex = fromIndex + 1; toIndex < numTiers; toIndex++) {
                Tier toTier = tiers[toIndex];
                Identifier itemId = Utils.id(fromTier.getId().getPath() + "_to_" + toTier.getId().getPath() + "_conversion_kit");
                Item.Settings properties = fromTier.getItemSettings()
                                                   .andThen(toTier.getItemSettings())
                                                   .apply(new Item.Settings().group(BaseCommon.GROUP).maxCount(16));
                Item kit = new StorageConversionKit(properties, fromTier.getId(), toTier.getId());
                items[index++] = new ItemRegistryEntry(itemId, kit);
            }
        }
    }

    private static int factorial(int value) {
        if (value == 1) {
            return value;
        }
        return value * factorial(value - 1);
    }
}
