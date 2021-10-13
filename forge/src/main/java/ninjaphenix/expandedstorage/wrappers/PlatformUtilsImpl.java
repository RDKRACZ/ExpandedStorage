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
package ninjaphenix.expandedstorage.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import ninjaphenix.expandedstorage.ForgeChestProperties;
import ninjaphenix.expandedstorage.Utils;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;

import java.util.function.Supplier;

final class PlatformUtilsImpl extends PlatformUtils {
    PlatformUtilsImpl() {
        super(FMLLoader.getDist() == Dist.CLIENT, true);
    }

    @Override
    public CreativeModeTab createTab(Supplier<ItemStack> icon) {
        return new CreativeModeTab(Utils.MOD_ID) {
            @Override
            public ItemStack makeIcon() {
                return icon.get();
            }
        };
    }

    @Override
    public Object createGenericItemAccess(AbstractOpenableStorageBlockEntity entity) {
        return new IItemHandlerModifiable() {
            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                entity.getItems().set(slot, stack);
                if (stack.getCount() > this.getSlotLimit(slot)) {
                    stack.setCount(this.getSlotLimit(slot));
                }
                entity.setChanged();
            }

            @Override
            public int getSlots() {
                return entity.getItems().size();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return entity.getItems().get(slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack stackInSlot = entity.getItems().get(slot);
                if (stackInSlot.isEmpty()) {
                    int limit = this.getSlotLimit(slot);
                    if (stack.getCount() > limit) {
                        if (!simulate) {
                            ItemStack newStack = stack.copy();
                            newStack.setCount(limit);
                            entity.getItems().set(slot, newStack);
                            entity.setChanged();
                        }
                        return simulate ? stack.copy().split(stack.getCount() - limit) : stack.split(stack.getCount() - limit);
                    } else {
                        if (!simulate) {
                            entity.getItems().set(slot, stack.copy());
                            entity.setChanged();
                        }
                    }
                } else if (ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
                    int limit = Math.min(stackInSlot.getMaxStackSize(), this.getSlotLimit(slot));
                    int diff = limit - stackInSlot.getCount();
                    if (diff != 0) {
                        if (stack.getCount() > diff) {
                            if (!simulate) {
                                stackInSlot.setCount(limit);
                                entity.setChanged();
                            }
                            return simulate ? stack.copy().split(stack.getCount() - diff) : stack.split(stack.getCount() - diff);
                        } else {
                            if (!simulate) {
                                stackInSlot.setCount(stackInSlot.getCount() + stack.getCount());
                                entity.setChanged();
                            }
                        }
                    } else {
                        return stack;
                    }
                } else {
                    return stack;
                }
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stackInSlot = entity.getItems().get(slot);
                if (stackInSlot.isEmpty()) {
                    return ItemStack.EMPTY;
                } else if (amount >= stackInSlot.getCount()) {
                    if (!simulate) {
                        entity.getItems().set(slot, ItemStack.EMPTY);
                    }
                    return stackInSlot;
                } else {
                    ItemStack copy = simulate ? stackInSlot.copy() : stackInSlot;
                    return copy.split(amount);
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return true;
            }
        };
    }

    @Override
    public Object createChestItemAccess(Level world, BlockState state, BlockPos pos, Direction side) {
        return ForgeChestProperties.createItemHandler(world, state, pos);
    }
}
