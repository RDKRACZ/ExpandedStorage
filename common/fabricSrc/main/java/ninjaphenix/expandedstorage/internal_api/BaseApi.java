package ninjaphenix.expandedstorage.internal_api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.BaseImpl;
import ninjaphenix.expandedstorage.internal_api.block.AbstractStorageBlock;
import ninjaphenix.expandedstorage.internal_api.item.BlockUpgradeBehaviour;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import java.util.Map;
import java.util.function.Predicate;

@Internal
@Experimental
public interface BaseApi {
    static BaseApi getInstance() {
        return BaseImpl.getInstance();
    }

    /**
     * Sets the ExpandedStorage creative tab icon only if it is more suitable than previously supplied icons.
     *
     * @param suitability Any integer between 1 and 949 ( inclusive ), you should not override ExpandedStorage's icon.
     */
    void offerTabIcon(Item tabIcon, int suitability);

    /**
     * Define a new upgrade path, will register all necessary upgrade items excluding duplicates.
     *
     * @param addingMod Friendly mod name for upgrade item tooltip
     * @param tiers     Storage block tiers in order of upgrade path
     */
    void defineTierUpgradePath(Text addingMod, Tier... tiers);

    @Internal
    ItemStack tabIcon();

    BlockUpgradeBehaviour getBlockUpgradeBehaviour(Block block);

    void defineBlockUpgradeBehaviour(Predicate<Block> target, BlockUpgradeBehaviour behaviour);

    @Internal
    void register(Identifier id, Item item);

    /**
     * @deprecated Will be removed with no replacement.
     */
    @Deprecated
    @ScheduledForRemoval
    void registerTieredBlock(AbstractStorageBlock block);

    /**
     * @deprecated Will be removed with no replacement.
     */
    @Deprecated
    @ScheduledForRemoval
    AbstractStorageBlock getTieredBlock(Identifier blockType, Identifier tier);

    @Internal
    Map<Identifier, Item> getAndClearItems();
}
