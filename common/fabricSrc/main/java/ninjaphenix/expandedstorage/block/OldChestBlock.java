package ninjaphenix.expandedstorage.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.OldChestCommon;
import ninjaphenix.expandedstorage.block.misc.OldChestBlockEntity;
import ninjaphenix.expandedstorage.internal_api.block.AbstractChestBlock;

public final class OldChestBlock extends AbstractChestBlock<OldChestBlockEntity> {
    public OldChestBlock(Settings properties, Identifier blockId, Identifier blockTier,
                         Identifier openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
    }

    @Override
    protected BlockEntityType<OldChestBlockEntity> getBlockEntityType() {
        return OldChestCommon.getBlockEntityType();
    }

    @Override
    public Identifier getBlockType() {
        return OldChestCommon.BLOCK_TYPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new OldChestBlockEntity(OldChestCommon.getBlockEntityType(), pos, state);
    }
}
