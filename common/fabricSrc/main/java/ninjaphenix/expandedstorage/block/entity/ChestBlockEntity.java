package ninjaphenix.expandedstorage.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.block.entity.extendable.OpenableBlockEntity;
import ninjaphenix.expandedstorage.block.strategies.ItemAccess;
import ninjaphenix.expandedstorage.block.strategies.Lockable;

import java.util.function.Function;

public class ChestBlockEntity extends OldChestBlockEntity {
    public ChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId,
                            Function<OpenableBlockEntity, ItemAccess> access, Function<OpenableBlockEntity, Lockable> lockable) {
        super(type, pos, state, blockId, access, lockable);
    }

    public int getLidOpenness(float delta) {
        return 0;
    }
}
