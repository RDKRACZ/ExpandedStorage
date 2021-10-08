package ninjaphenix.expandedstorage.internal_api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import ninjaphenix.expandedstorage.BaseImpl;
import ninjaphenix.expandedstorage.internal_api.block.AbstractStorageBlock;
import ninjaphenix.expandedstorage.internal_api.item.BlockUpgradeBehaviour;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static org.jetbrains.annotations.ApiStatus.Experimental;
import static org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public interface BaseApi {
    static BaseApi getInstance() {
        return BaseImpl.getInstance();
    }

    /**
     * Define a new upgrade path, will register all necessary upgrade items excluding duplicates.
     *
     * @param addingMod Friendly mod name for upgrade item tooltip
     * @param tiers     Storage block tiers in order of upgrade path
     */
    void defineTierUpgradePath(Component addingMod, Tier... tiers);

    Optional<BlockUpgradeBehaviour> getBlockUpgradeBehaviour(Block block);

    void defineBlockUpgradeBehaviour(Predicate<Block> target, BlockUpgradeBehaviour behaviour);

    @Internal
    void register(ResourceLocation id, Item item);

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
    AbstractStorageBlock getTieredBlock(ResourceLocation blockType, ResourceLocation tier);

    @Internal
    Map<ResourceLocation, Item> getAndClearItems();
}
