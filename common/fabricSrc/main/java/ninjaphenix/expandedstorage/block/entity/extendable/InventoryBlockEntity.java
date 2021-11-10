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
package ninjaphenix.expandedstorage.block.entity.extendable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public abstract class InventoryBlockEntity extends OpenableBlockEntity {
    private final int[] availableSlots;
    private final DefaultedList<ItemStack> items;
    private final SidedInventory inventory = new SidedInventory() {
        @Override
        public int[] getAvailableSlots(Direction side) {
            // todo: make this lazy
            return availableSlots;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return true;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return true;
        }

        @Override
        public int size() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (stack.isEmpty()) continue;
                return false;
            }
            return true;
        }

        @Override
        public ItemStack getStack(int slot) {
            return items.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            ItemStack stack = Inventories.splitStack(items, slot, amount);
            if (!stack.isEmpty()) this.markDirty();
            return stack;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return Inventories.removeStack(items, slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (stack.getCount() > this.getMaxCountPerStack()) stack.setCount(this.getMaxCountPerStack());
            items.set(slot, stack);
            this.markDirty();
        }

        @Override
        public void markDirty() {
            InventoryBlockEntity.this.markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {
            items.clear();
        }
    };

    public InventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId, int inventorySize) {
        super(type, pos, state, blockId);
        items = DefaultedList.ofSize(inventorySize, ItemStack.EMPTY);
        availableSlots = IntStream.range(0, inventorySize).toArray();
    }

    public final SidedInventory getInventory() {
        return inventory;
    }

    @Override
    public final DefaultedList<ItemStack> getItems() {
        return items;
    }

    protected abstract boolean shouldStateUpdateInvalidateItemAccess(BlockState oldState, BlockState newState);

    @Override // Could be a part of StrategyBlockEntity but is only used for implementors of InventoryBlockEntity
    @SuppressWarnings("deprecation")
    public void setCachedState(BlockState state) {
        BlockState oldState = this.getCachedState();
        super.setCachedState(state);
        if (this.shouldStateUpdateInvalidateItemAccess(oldState, state)) {
            this.getItemAccess().invalidate();
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, items);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        Inventories.writeNbt(tag, items);
        return tag;
    }
}

