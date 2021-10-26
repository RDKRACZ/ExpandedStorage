/**
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
package ninjaphenix.expandedstorage.block.misc.strategies.temp_be;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.block.misc.strategies.ItemAccess;
import ninjaphenix.expandedstorage.block.misc.strategies.Lockable;
import ninjaphenix.expandedstorage.block.misc.strategies.Nameable;
import ninjaphenix.expandedstorage.block.misc.strategies.Observable;
import ninjaphenix.expandedstorage.block.misc.strategies.temp_be.extendable.ExposedInventoryBlockEntity;

public final class MiniChestBlockEntity extends ExposedInventoryBlockEntity {
    private ItemStack content = ItemStack.EMPTY;

    public MiniChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId,
                                ItemAccess access, Lockable lockable) {
        super(type, pos, state, blockId);
        this.setItemAccess(access);
        this.setLock(lockable);
        this.setName(new Nameable.Mutable(((AbstractOpenableStorageBlock) state.getBlock()).getInventoryTitle())); // (MiniChestBlock)
        this.setObservable(Observable.NOT);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot != 0) throw new IndexOutOfBoundsException();
        return content;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot == 0 && amount > 0) {
            ItemStack returnValue = content.split(amount);
            if (!returnValue.isEmpty()) this.markDirty();
            return returnValue;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot == 0) {
            ItemStack returnValue = content;
            content = ItemStack.EMPTY;
            return returnValue;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) throw new IndexOutOfBoundsException();
        if (stack.getCount() > this.getMaxCountPerStack()) stack.setCount(this.getMaxCountPerStack());
        content = stack;
        this.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        content = ItemStack.EMPTY;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        content = ItemStack.fromNbt(tag.getCompound("content"));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.put("content", content.writeNbt(new NbtCompound()));
        return tag;
    }
}
