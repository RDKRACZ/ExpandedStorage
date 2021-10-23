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

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.FabricChestProperties;
import ninjaphenix.expandedstorage.Utils;
import ninjaphenix.expandedstorage.block.misc.AbstractAccessibleStorageBlockEntity;

import java.util.function.Supplier;

public final class PlatformUtilsImpl extends PlatformUtils {
    PlatformUtilsImpl() {
        super(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT, false);
    }

    @Override
    public ItemGroup createTab(Supplier<ItemStack> icon) { // Hopefully fabric api gets rid of this builder in favour of transitive AW.
        FabricItemGroupBuilder.build(new Identifier("dummy"), null); // Fabric API is dumb.
        return new ItemGroup(ItemGroup.GROUPS.length - 1, Utils.MOD_ID) {
            @Override
            public ItemStack createIcon() {
                return icon.get();
            }
        };
    }

    @Override
    public Object createGenericItemAccess(AbstractAccessibleStorageBlockEntity entity) {
        DefaultedList<ItemStack> items = entity.getItems();
        Inventory wrapped = entity.getInventory();
        Inventory transferApiInventory = new Inventory() {
            @Override
            public int size() {
                return wrapped.size();
            }

            @Override
            public boolean isEmpty() {
                return wrapped.isEmpty();
            }

            @Override
            public ItemStack getStack(int slot) {
                return wrapped.getStack(slot);
            }

            @Override
            public ItemStack removeStack(int slot, int amount) {
                return Inventories.splitStack(items, slot, amount);
            }

            @Override
            public ItemStack removeStack(int slot) {
                return wrapped.removeStack(slot);
            }

            @Override
            public void setStack(int slot, ItemStack stack) {
                items.set(slot, stack);
                if (stack.getCount() > this.getMaxCountPerStack()) {
                    stack.setCount(this.getMaxCountPerStack());
                }
            }

            @Override
            public void markDirty() {
                wrapped.markDirty();
            }

            @Override
            public boolean canPlayerUse(PlayerEntity player) {
                return wrapped.canPlayerUse(player);
            }

            @Override
            public void clear() {
                wrapped.clear();
            }

            @Override
            public void onOpen(PlayerEntity player) {
                wrapped.onOpen(player);
            }

            @Override
            public void onClose(PlayerEntity player) {
                wrapped.onClose(player);
            }
        };
        //noinspection UnstableApiUsage,deprecation
        return InventoryStorage.of(transferApiInventory, null);
    }

    @Override
    public Object createChestItemAccess(World world, BlockState state, BlockPos pos, Direction side) {
        return FabricChestProperties.createItemStorage(world, state, pos);
    }
}
