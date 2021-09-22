package ninjaphenix.expandedstorage.internal_api;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.BaseImpl;
import ninjaphenix.expandedstorage.internal_api.block.AbstractStorageBlock;
import ninjaphenix.expandedstorage.internal_api.item.BlockUpgradeBehaviour;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import java.util.function.Predicate;

@Internal
@Experimental
public interface BaseApi {
    static BaseApi getInstance() {
        return BaseImpl.getInstance();
    }

    BlockUpgradeBehaviour getBlockUpgradeBehaviour(Block block);

    void defineBlockUpgradeBehaviour(Predicate<Block> target, BlockUpgradeBehaviour behaviour);

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
}
