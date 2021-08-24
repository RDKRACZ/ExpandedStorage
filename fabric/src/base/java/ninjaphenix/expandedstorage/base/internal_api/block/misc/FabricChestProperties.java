package ninjaphenix.expandedstorage.base.internal_api.block.misc;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableObject;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.expandedstorage.base.internal_api.block.AbstractChestBlock;

import java.util.List;
import java.util.Optional;

public final class FabricChestProperties {
    public static final String LOCK_TAG_KEY = "HTM_Lock";

    public static final Property<AbstractOpenableStorageBlockEntity, HTMContainerLock> LOCK_PROPERTY = new Property<>() {
        @Override
        public HTMContainerLock get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            LockableObject firstLockable = (LockableObject) first;
            LockableObject secondLockable = (LockableObject) second;
            if (firstLockable.getLock().isLocked() || !secondLockable.getLock().isLocked()) {
                return firstLockable.getLock();
            }
            return secondLockable.getLock();
        }

        @Override
        public HTMContainerLock get(AbstractOpenableStorageBlockEntity single) {
            return ((LockableObject) single).getLock();
        }
    };

    public static final Property<AbstractOpenableStorageBlockEntity, Optional<BlockEntity>> UNLOCKED_BE_PROPERTY = new Property<>() {
        @Override
        public Optional<BlockEntity> get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            LockableObject firstLockable = (LockableObject) first;
            if (!firstLockable.getLock().isLocked()) {
                return Optional.of(first);
            }
            LockableObject secondLockable = (LockableObject) second;
            if (!secondLockable.getLock().isLocked()) {
                return Optional.of(second);
            }
            return Optional.empty();
        }

        @Override
        public Optional<BlockEntity> get(AbstractOpenableStorageBlockEntity single) {
            return Optional.empty();
        }
    };

    public static final Property<AbstractOpenableStorageBlockEntity, Optional<Storage<ItemVariant>>> INVENTORY_GETTER = new Property<>() {
        @Override
        public Optional<Storage<ItemVariant>> get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            return Optional.of(new CombinedStorage<>(List.of(AbstractOpenableStorageBlockEntity.createGenericItemStorage(first),
                    AbstractOpenableStorageBlockEntity.createGenericItemStorage(second))));
        }

        @Override
        public Optional<Storage<ItemVariant>> get(AbstractOpenableStorageBlockEntity single) {
            return Optional.of(AbstractOpenableStorageBlockEntity.createGenericItemStorage(single));
        }
    };


    public static Optional<Storage<ItemVariant>> createItemStorage(Level level, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, level, pos, true).get(FabricChestProperties.INVENTORY_GETTER);
        }
        return Optional.empty();
    }
}
