package ninjaphenix.expandedstorage.chest.mixin;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.expandedstorage.base.internal_api.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.chest.compat.HTMProperties;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

/**
 * Makes use of Hey That's Mine's API which is licensed MIT
 * https://www.curseforge.com/minecraft/mc-mods/htm
 */
@Mixin(AbstractChestBlock.class)
public abstract class HTMChestSupport implements LockableChestBlock {
    @Override
    public HTMContainerLock getLockAt(BlockState state, Level level, BlockPos pos) {
        return self().createCombinedPropertyGetter(state, level, pos, true).apply(HTMProperties.LOCK_GETTER);
    }

    // Seems to be used to synchronize lock between both parts of chest.
    @Override
    public Optional<BlockEntity> getUnlockedPart(BlockState state, Level level, BlockPos pos) {
        return self().createCombinedPropertyGetter(state, level, pos, true).apply(HTMProperties.UNLOCKED_BE_GETTER);
    }

    private AbstractChestBlock<AbstractOpenableStorageBlockEntity> self() {
        //noinspection ConstantConditions, unchecked
        return (AbstractChestBlock<AbstractOpenableStorageBlockEntity>) (Object) this;
    }
}
