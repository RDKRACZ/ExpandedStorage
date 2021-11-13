package ninjaphenix.expandedstorage.mixin;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.block.entity.extendable.OpenableBlockEntity;
import ninjaphenix.expandedstorage.block.entity.extendable.StrategyBlockEntity;
import ninjaphenix.expandedstorage.compat.htm.HTMLockable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OpenableBlockEntity.class)
public abstract class HTMLockableBlockEntityCompat extends StrategyBlockEntity implements LockableObject {
    public HTMLockableBlockEntityCompat(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId) {
        super(type, pos, state, blockId);
    }

    @Override
    public void setLock(HTMContainerLock lock) {
        ((HTMLockable) this.getLockable()).setLock(lock);
    }

    @Override
    public HTMContainerLock getLock() {
        return ((HTMLockable) this.getLockable()).getLock();
    }
}
