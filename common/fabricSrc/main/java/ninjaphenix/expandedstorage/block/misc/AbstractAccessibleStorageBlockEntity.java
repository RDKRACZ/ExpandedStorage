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
package ninjaphenix.expandedstorage.block.misc;

import com.google.common.base.Suppliers;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.AbstractStorageBlock;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class AbstractAccessibleStorageBlockEntity<T extends AbstractStorageBlock> extends AbstractStorageBlockEntity<T> {
    private Supplier<Object> itemAccess;

    public AbstractAccessibleStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, Identifier blockId) {
        super(blockEntityType, pos, state, blockId);
    }

    public static Object getItemAccess(World world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        if (blockEntity != null) {
            AbstractAccessibleStorageBlockEntity entity = (AbstractAccessibleStorageBlockEntity) blockEntity;
            if (entity.itemAccess == null) {
                entity.itemAccess = Suppliers.memoize(() -> entity.createItemAccess(world, state, pos, direction));
            }
            return entity.itemAccess.get();
        }
        return null;

    }

    protected Object createItemAccess(World world, BlockState state, BlockPos pos, @Nullable Direction side) {
        return PlatformUtils.getInstance().createGenericItemAccess(this);
    }

    protected void invalidateItemAccess() {
        itemAccess = null;
    }

    public abstract DefaultedList<ItemStack> getItems();

    public abstract Inventory getInventory();
}
