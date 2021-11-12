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
package ninjaphenix.expandedstorage.block.strategies;

import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Lockable {
    Lockable NOT_LOCKABLE = new Lockable() {
        @Override
        public void writeLock(NbtCompound tag) {

        }

        @Override
        public void readLock(NbtCompound tag) {

        }

        @Override
        public boolean canPlayerOpenLock(ServerPlayerEntity player) {
            return true;
        }
    };

    void writeLock(NbtCompound tag);
    void readLock(NbtCompound tag);
    boolean canPlayerOpenLock(ServerPlayerEntity player);

    class Basic implements Lockable {
        ContainerLock lock = ContainerLock.EMPTY;
        @Override
        public void writeLock(NbtCompound tag) {
            lock.writeNbt(tag);
        }

        @Override
        public void readLock(NbtCompound tag) {
            lock = ContainerLock.fromNbt(tag);
        }

        @Override
        public boolean canPlayerOpenLock(ServerPlayerEntity player) {
            return lock == ContainerLock.EMPTY || !player.isSpectator() && lock.canOpen(player.getMainHandStack());
        }
    }
}
