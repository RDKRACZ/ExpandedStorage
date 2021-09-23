package ninjaphenix.expandedstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

public final class ForgeChestProperties {
    public static final DoubleBlockCombiner.Combiner<AbstractOpenableStorageBlockEntity, Object> INVENTORY_GETTER = new DoubleBlockCombiner.Combiner<>() {
        @Override
        public Object acceptDouble(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            return new CombinedInvWrapper(
                    (IItemHandlerModifiable) PlatformUtils.getInstance().createGenericItemAccess(first),
                    (IItemHandlerModifiable) PlatformUtils.getInstance().createGenericItemAccess(second)
            );
        }

        @Override
        public Object acceptSingle(AbstractOpenableStorageBlockEntity single) {
            return PlatformUtils.getInstance().createGenericItemAccess(single);
        }

        @Override
        public Object acceptNone() {
            return null;
        }
    };

    public static Object createItemHandler(Level level, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            //noinspection unchecked
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, level, pos, false).apply(ForgeChestProperties.INVENTORY_GETTER);
        }
        return null;
    }
}
