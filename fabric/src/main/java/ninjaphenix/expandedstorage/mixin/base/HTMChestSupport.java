package ninjaphenix.expandedstorage.mixin.base;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.base.internal_api.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.FabricChestProperties;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(AbstractChestBlock.class)
public abstract class HTMChestSupport implements LockableChestBlock {
    @Override
    public HTMContainerLock getLockAt(BlockState state, World level, BlockPos pos) {
        return AbstractChestBlock.createPropertyRetriever(self(), state, level, pos, true).get(FabricChestProperties.LOCK_PROPERTY);
    }

    // Seems to be used to synchronize lock between both parts of chest.
    @Override
    public Optional<BlockEntity> getUnlockedPart(BlockState state, World level, BlockPos pos) {
        return AbstractChestBlock.createPropertyRetriever(self(), state, level, pos, true).get(FabricChestProperties.UNLOCKED_BE_PROPERTY);
    }

    private AbstractChestBlock<AbstractOpenableStorageBlockEntity> self() {
        //noinspection ConstantConditions, unchecked
        return (AbstractChestBlock<AbstractOpenableStorageBlockEntity>) (Object) this;
    }
}
