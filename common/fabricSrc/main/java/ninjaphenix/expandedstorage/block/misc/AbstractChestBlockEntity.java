package ninjaphenix.expandedstorage.block.misc;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.Nullable;

public class AbstractChestBlockEntity extends AbstractOpenableStorageBlockEntity {
    public AbstractChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, Identifier blockId) {
        super(blockEntityType, pos, state, blockId);
    }

    @Override
    protected Object createItemAccess(World level, BlockState state, BlockPos pos, @Nullable Direction side) {
        return PlatformUtils.getInstance().createChestItemAccess(world, state, pos, side);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.itemAccess = null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.itemAccess = null;
    }
}
