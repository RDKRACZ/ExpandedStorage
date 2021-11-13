package ninjaphenix.expandedstorage.compat.htm;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableObject;
import net.minecraft.block.entity.BlockEntity;
import ninjaphenix.expandedstorage.block.entity.OldChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.Property;

public final class HTMChestProperties {

    private HTMChestProperties() {
        throw new IllegalStateException("Should not instantiate this class.");
    }

    public static final Property<OldChestBlockEntity, HTMContainerLock> LOCK_PROPERTY = new Property<>() {
        @Override
        public HTMContainerLock get(OldChestBlockEntity first, OldChestBlockEntity second) {
            LockableObject firstLockable = (LockableObject) first;
            LockableObject secondLockable = (LockableObject) second;
            if (firstLockable.getLock().isLocked() || !secondLockable.getLock().isLocked()) {
                return firstLockable.getLock();
            }
            return secondLockable.getLock();
        }

        @Override
        public HTMContainerLock get(OldChestBlockEntity single) {
            return ((LockableObject) single).getLock();
        }
    };

    public static final Property<OldChestBlockEntity, BlockEntity> UNLOCKED_BE_PROPERTY = new Property<>() {
        @Override
        public BlockEntity get(OldChestBlockEntity first, OldChestBlockEntity second) {
            LockableObject firstLockable = (LockableObject) first;
            if (!firstLockable.getLock().isLocked()) {
                return first;
            }
            LockableObject secondLockable = (LockableObject) second;
            if (!secondLockable.getLock().isLocked()) {
                return second;
            }
            return null;
        }

        @Override
        public BlockEntity get(OldChestBlockEntity single) {
            return null;
        }
    };
}
