package ninjaphenix.expandedstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.block.misc.AbstractChestBlockEntity;

public final class OldChestBlock extends AbstractChestBlock<AbstractChestBlockEntity> {
    public OldChestBlock(Properties properties, ResourceLocation blockId, ResourceLocation blockTier,
                         ResourceLocation openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
    }

    @Override
    protected BlockEntityType<AbstractChestBlockEntity> getBlockEntityType() {
        return Common.getOldChestBlockEntityType();
    }

    @Override
    public ResourceLocation getBlockType() {
        return Common.OLD_CHEST_BLOCK_TYPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbstractChestBlockEntity(Common.getOldChestBlockEntityType(), pos, state, this.getBlockId());
    }
}
