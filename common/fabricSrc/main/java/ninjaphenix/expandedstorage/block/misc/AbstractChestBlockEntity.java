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

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.container_library.api.helpers.VariableInventory;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.Nullable;

public class AbstractChestBlockEntity extends AbstractOpenableStorageBlockEntity {
    public AbstractChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, Identifier blockId, boolean observable) {
        super(blockEntityType, pos, state, blockId, observable);
    }

    @Override
    protected Object createItemAccess(World level, BlockState state, BlockPos pos, @Nullable Direction side) {
        return PlatformUtils.getInstance().createChestItemAccess(world, state, pos, side);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.itemAccess = null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.itemAccess = null;
    }

    @Override
    protected boolean isThis(Inventory inventory) {
        return super.isThis(inventory) || inventory instanceof VariableInventory variableInventory && variableInventory.containsPart(this.getInventory());
    }
}
