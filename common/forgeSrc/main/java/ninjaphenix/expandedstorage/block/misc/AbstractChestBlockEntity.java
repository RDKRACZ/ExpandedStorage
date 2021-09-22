package ninjaphenix.expandedstorage.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.Nullable;

public class AbstractChestBlockEntity extends AbstractOpenableStorageBlockEntity {
    public AbstractChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, ResourceLocation blockId) {
        super(blockEntityType, pos, state, blockId);
    }

    @Override
    protected Object createItemAccess(Level level, BlockState state, BlockPos pos, @Nullable Direction side) {
        return PlatformUtils.getInstance().createChestItemAccess(level, state, pos, side);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.itemAccess = null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        this.itemAccess = null;
    }
}
