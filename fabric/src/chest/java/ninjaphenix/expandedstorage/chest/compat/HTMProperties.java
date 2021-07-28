package ninjaphenix.expandedstorage.chest.compat;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableObject;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;

import java.util.Optional;

/**
 * Makes use of Hey That's Mine's API which is licensed MIT
 * https://www.curseforge.com/minecraft/mc-mods/htm
 */
public final class HTMProperties {
    public static final String LOCK_TAG_KEY = "HTM_Lock";
    public static final DoubleBlockCombiner.Combiner<AbstractOpenableStorageBlockEntity, HTMContainerLock> LOCK_GETTER = new DoubleBlockCombiner.Combiner<>() {
        @Override
        public HTMContainerLock acceptDouble(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            LockableObject firstLockable = (LockableObject) first;
            LockableObject secondLockable = (LockableObject) second;
            if (firstLockable.getLock().isLocked() || !secondLockable.getLock().isLocked()) {
                return firstLockable.getLock();
            }
            return secondLockable.getLock();
        }

        @Override
        public HTMContainerLock acceptSingle(AbstractOpenableStorageBlockEntity single) {
            return ((LockableObject) single).getLock();
        }

        @Override
        public HTMContainerLock acceptNone() {
            return null;
        }
    };
    public static final DoubleBlockCombiner.Combiner<AbstractOpenableStorageBlockEntity,Optional<BlockEntity>> UNLOCKED_BE_GETTER = new DoubleBlockCombiner.Combiner<AbstractOpenableStorageBlockEntity, Optional<BlockEntity>>() {
        @Override
        public Optional<BlockEntity> acceptDouble(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
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
        public Optional<BlockEntity> acceptSingle(AbstractOpenableStorageBlockEntity single) {
            return Optional.empty();
        }

        @Override
        public Optional<BlockEntity> acceptNone() {
            return Optional.empty();
        }
    };
}
