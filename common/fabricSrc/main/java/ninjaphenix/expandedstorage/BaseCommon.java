package ninjaphenix.expandedstorage;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.item.StorageMutator;

public final class BaseCommon {
    private static final int ICON_SUITABILITY = 0;

    private BaseCommon() {

    }

    static void initialize() {
        BaseApi.getInstance().offerTabIcon(Items.CHEST, ICON_SUITABILITY);
        BaseApi.getInstance().defineTierUpgradePath(Utils.translation("itemGroup.expandedstorage"), Utils.WOOD_TIER, Utils.IRON_TIER,
                Utils.GOLD_TIER, Utils.DIAMOND_TIER, Utils.OBSIDIAN_TIER, Utils.NETHERITE_TIER);
        BaseApi.getInstance().register(Utils.resloc("chest_mutator"), new StorageMutator(new Item.Settings().maxCount(1).group(Utils.TAB)));
    }

    public static Identifier registerStat(Identifier stat) {
        Identifier rv = Registry.register(Registry.CUSTOM_STAT, stat, stat); // Forge doesn't provide registries for stats
        Stats.CUSTOM.getOrCreateStat(rv);
        return rv;
    }
}
