package ninjaphenix.expandedstorage.base.internal_api.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import ninjaphenix.expandedstorage.base.internal_api.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.base.internal_api.inventory.CombinedIItemHandlerModifiable;

import java.util.Optional;

public final class ForgeChestProperties {
    public static final Property<AbstractOpenableStorageBlockEntity, Optional<IItemHandlerModifiable>> INVENTORY_GETTER = new Property<>() {
        @Override
        public Optional<IItemHandlerModifiable> get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            return Optional.of(new CombinedIItemHandlerModifiable(
                    AbstractOpenableStorageBlockEntity.createGenericItemHandler(first),
                    AbstractOpenableStorageBlockEntity.createGenericItemHandler(second)
            ));
        }

        @Override
        public Optional<IItemHandlerModifiable> get(AbstractOpenableStorageBlockEntity single) {
            return Optional.of(AbstractOpenableStorageBlockEntity.createGenericItemHandler(single));
        }
    };

    public static Optional<IItemHandlerModifiable> createItemHandler(Level level, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, level, pos, false).get(ForgeChestProperties.INVENTORY_GETTER);
        }
        return Optional.empty();
    }
}
