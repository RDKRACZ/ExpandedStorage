package ninjaphenix.expandedstorage.old_chest.block.misc;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.FabricChestProperties;
import ninjaphenix.expandedstorage.old_chest.block.OldChestBlock;
import org.jetbrains.annotations.Nullable;

public final class OldChestBlockEntity extends AbstractOpenableStorageBlockEntity {
    public OldChestBlockEntity(BlockEntityType<OldChestBlockEntity> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state, ((OldChestBlock) state.getBlock()).getBlockId());
    }

    @Override
    protected Storage<ItemVariant> createItemStorage(World level, BlockState state, BlockPos pos, @Nullable Direction side) {
        return FabricChestProperties.createItemStorage(level, state, pos).orElse(AbstractOpenableStorageBlockEntity.createGenericItemStorage(this));
    }
}
