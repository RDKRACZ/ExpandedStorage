package ninjaphenix.expandedstorage.internal_api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractStorageBlockEntity;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

@Internal
@Experimental
public abstract class AbstractStorageBlock extends Block {
    private final Identifier blockId;
    private final Identifier blockTier;

    public AbstractStorageBlock(Settings properties, Identifier blockId, Identifier blockTier) {
        super(properties);
        this.blockId = blockId;
        this.blockTier = blockTier;
    }

    public abstract Identifier getBlockType();

    public final Identifier getBlockId() {
        return blockId;
    }

    public final Identifier getBlockTier() {
        return blockTier;
    }

    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof AbstractStorageBlockEntity entity && stack.hasCustomName()) {
            entity.setMenuTitle(stack.getName());
        }
    }
}
