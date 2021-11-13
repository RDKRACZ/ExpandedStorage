package ninjaphenix.expandedstorage.compat.htm;

import com.github.fabricservertools.htm.HTMContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import ninjaphenix.expandedstorage.block.strategies.Lockable;

public final class HTMLockable extends Lockable.Basic {
    public static final String LOCK_TAG_KEY = "HTM_Lock";
    private HTMContainerLock lock = new HTMContainerLock();

    @Override
    public void writeLock(NbtCompound tag) {
        super.writeLock(tag);
        NbtCompound subTag = new NbtCompound();
        lock.toTag(subTag);
        tag.put(HTMLockable.LOCK_TAG_KEY, subTag);
    }

    @Override
    public void readLock(NbtCompound tag) {
        super.readLock(tag);
        if (tag.contains(HTMLockable.LOCK_TAG_KEY, NbtElement.COMPOUND_TYPE)) {
            lock.fromTag(tag.getCompound(HTMLockable.LOCK_TAG_KEY));
        }
    }

    @Override
    public boolean canPlayerOpenLock(ServerPlayerEntity player) {
        return !lock.isLocked() && super.canPlayerOpenLock(player) || lock.isLocked() && lock.canOpen(player);
    }

    public HTMContainerLock getLock() {
        return lock;
    }

    public void setLock(HTMContainerLock lock) {
        this.lock = lock;
    }
}
