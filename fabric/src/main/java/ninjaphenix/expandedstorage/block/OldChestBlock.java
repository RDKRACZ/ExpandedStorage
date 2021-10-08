package ninjaphenix.expandedstorage.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ninjaphenix.expandedstorage.CommonMain;
import ninjaphenix.expandedstorage.block.misc.OldChestBlockEntity;

public final class OldChestBlock extends AbstractChestBlock<OldChestBlockEntity> {
    public OldChestBlock(Properties properties, ResourceLocation blockId, ResourceLocation blockTier,
                         ResourceLocation openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
    }

    @Override
    protected BlockEntityType<OldChestBlockEntity> getBlockEntityType() {
        return CommonMain.getOldChestBlockEntityType();
    }

    @Override
    public ResourceLocation getBlockType() {
        return CommonMain.OLD_CHEST_BLOCK_TYPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter getter) {
        return new OldChestBlockEntity(CommonMain.getOldChestBlockEntityType(), this.getBlockId());
    }
}
