package ninjaphenix.expandedstorage.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.block.misc.OldChestBlockEntity;
import ninjaphenix.expandedstorage.internal_api.block.AbstractChestBlock;

public final class OldChestBlock extends AbstractChestBlock<OldChestBlockEntity> {
    public OldChestBlock(Settings properties, Identifier blockId, Identifier blockTier,
                         Identifier openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
    }

    @Override
    protected BlockEntityType<OldChestBlockEntity> getBlockEntityType() {
        return Common.getOldChestBlockEntityType();
    }

    @Override
    public Identifier getBlockType() {
        return Common.OLD_CHEST_BLOCK_TYPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new OldChestBlockEntity(Common.getOldChestBlockEntityType(), pos, state);
    }
}
