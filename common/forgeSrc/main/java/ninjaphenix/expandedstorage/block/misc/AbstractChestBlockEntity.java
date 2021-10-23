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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.container_library.api.helpers.VariableInventory;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.Nullable;

public class AbstractChestBlockEntity extends AbstractOpenableStorageBlockEntity {
    public AbstractChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, ResourceLocation blockId, boolean observable) {
        super(blockEntityType, pos, state, blockId, observable);
    }

    @Override
    protected Object createItemAccess(Level level, BlockState state, BlockPos pos, @Nullable Direction side) {
        return PlatformUtils.getInstance().createChestItemAccess(level, state, pos, side);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (PlatformUtils.getInstance().isForge()) {
            this.invalidateCache();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        this.invalidateCache();
    }

    @Override
    protected boolean isThis(Container inventory) {
        return super.isThis(inventory) || inventory instanceof VariableInventory variableInventory && variableInventory.containsPart(this.getInventory());
    }
}
