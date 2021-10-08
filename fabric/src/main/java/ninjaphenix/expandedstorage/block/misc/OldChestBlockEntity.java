package ninjaphenix.expandedstorage.block.misc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;

public final class OldChestBlockEntity extends AbstractOpenableStorageBlockEntity {
    public OldChestBlockEntity(BlockEntityType<OldChestBlockEntity> blockEntityType, ResourceLocation blockId) {
        super(blockEntityType, blockId);
    }

    @Override
    public Container getInventory() {
        if (this.getBlockState().getBlock() instanceof AbstractChestBlock<?> block) {
            return block.getContainer(this.getBlockState(), this.getLevel(), this.getBlockPos());
        }

        return null;
    }
}
