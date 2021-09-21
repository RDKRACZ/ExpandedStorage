package ninjaphenix.expandedstorage;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.block.AbstractStorageBlock;
import ninjaphenix.expandedstorage.internal_api.item.BlockUpgradeBehaviour;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import ninjaphenix.expandedstorage.item.StorageConversionKit;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class BaseImpl implements BaseApi {
    private static BaseImpl instance;
    private final Map<Predicate<Block>, BlockUpgradeBehaviour> BLOCK_UPGRADE_BEHAVIOURS = new HashMap<>();
    private final Map<Pair<Identifier, Identifier>, AbstractStorageBlock> BLOCKS = new HashMap<>();
    private Map<Identifier, Item> items = new LinkedHashMap<>();
    private Item tabIcon = Items.ENDER_CHEST;
    private int suitability = -1;

    private BaseImpl() {

    }

    public static BaseImpl getInstance() {
        if (BaseImpl.instance == null) {
            BaseImpl.instance = new BaseImpl();
        }
        return BaseImpl.instance;
    }

    @Override
    public BlockUpgradeBehaviour getBlockUpgradeBehaviour(Block block) {
        for (Map.Entry<Predicate<Block>, BlockUpgradeBehaviour> entry : BLOCK_UPGRADE_BEHAVIOURS.entrySet()) {
            if (entry.getKey().test(block)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void defineBlockUpgradeBehaviour(Predicate<Block> target, BlockUpgradeBehaviour behaviour) {
        BLOCK_UPGRADE_BEHAVIOURS.put(target, behaviour);
    }

    @Override
    public void defineTierUpgradePath(Text addingMod, Tier... tiers) {
        int numTiers = tiers.length;
        for (int fromIndex = 0; fromIndex < numTiers - 1; fromIndex++) {
            Tier fromTier = tiers[fromIndex];
            for (int toIndex = fromIndex + 1; toIndex < numTiers; toIndex++) {
                Tier toTier = tiers[toIndex];
                Identifier itemId = Utils.id(fromTier.getId().getPath() + "_to_" + toTier.getId().getPath() + "_conversion_kit");
                if (!items.containsKey(itemId)) {
                    Item.Settings properties = fromTier.getItemSettings()
                                                       .andThen(toTier.getItemSettings())
                                                       .apply(new Item.Settings().group(Utils.TAB).maxCount(16));
                    Item kit = new StorageConversionKit(properties, fromTier.getId(), toTier.getId());
                    this.register(itemId, kit);
                }
            }
        }
    }

    @Override
    @Internal
    public void register(Identifier itemId, Item item) {
        items.put(itemId, item);
    }

    @Override
    public void registerTieredBlock(AbstractStorageBlock block) {
        BLOCKS.putIfAbsent(new Pair<>(block.getBlockType(), block.getBlockTier()), block);
    }

    @Override
    public AbstractStorageBlock getTieredBlock(Identifier blockType, Identifier tier) {
        return BLOCKS.get(new Pair<>(blockType, tier));
    }

    @Override
    @Internal
    public Map<Identifier, Item> getAndClearItems() {
        Map<Identifier, Item> items = this.items;
        this.items = null;
        return items;
    }

    @Override
    public void offerTabIcon(Item tabIcon, int suitability) {
        if (this.suitability < suitability) {
            this.suitability = suitability;
            this.tabIcon = tabIcon;
        }
    }

    @Override
    @Internal
    public ItemStack tabIcon() {
        return new ItemStack(tabIcon);
    }
}
