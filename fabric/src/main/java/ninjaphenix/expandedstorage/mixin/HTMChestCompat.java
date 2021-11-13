package ninjaphenix.expandedstorage.mixin;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.compat.htm.HTMChestProperties;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(AbstractChestBlock.class)
public abstract class HTMChestCompat implements LockableChestBlock {
    @Override
    public HTMContainerLock getLockAt(BlockState state, World world, BlockPos pos) {
        //noinspection ConstantConditions
        return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock) (Object) this, state, world, pos, true).get(HTMChestProperties.LOCK_PROPERTY).orElse(null);
    }

    @Override
    public Optional<BlockEntity> getUnlockedPart(BlockState state, World world, BlockPos pos) {
        //noinspection ConstantConditions
        return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock) (Object) this, state, world, pos, true).get(HTMChestProperties.UNLOCKED_BE_PROPERTY);
    }

}
