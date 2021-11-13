/*
 * Copyright 2021 NinjaPhenix
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninjaphenix.expandedstorage.compat.htm;

import com.github.fabricservertools.htm.HTMContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import ninjaphenix.expandedstorage.block.misc.BasicLockable;

public final class HTMLockable extends BasicLockable {
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
        if (tag.contains(HTMLockable.LOCK_TAG_KEY, NbtElement.COMPOUND_TYPE))
            lock.fromTag(tag.getCompound(HTMLockable.LOCK_TAG_KEY));
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
