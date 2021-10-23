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
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractAccessibleStorageBlockEntity extends AbstractStorageBlockEntity {
    private Supplier<Object> itemAccess;

    public AbstractAccessibleStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public static Object getItemAccess(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        if (blockEntity != null) {
            AbstractAccessibleStorageBlockEntity entity = (AbstractAccessibleStorageBlockEntity) blockEntity;
            if (entity.itemAccess == null) {
                entity.itemAccess = Suppliers.memoize(() -> entity.createItemAccess(world, state, pos, direction));
            }
            return entity.itemAccess.get();
        }
        return null;

    }

    protected Object createItemAccess(Level world, BlockState state, BlockPos pos, @Nullable Direction side) {
        return PlatformUtils.getInstance().createGenericItemAccess(this);
    }

    protected void invalidateCache() {
        itemAccess = null;
    }

    public abstract NonNullList<ItemStack> getItems();

    public abstract Container getInventory();
}
