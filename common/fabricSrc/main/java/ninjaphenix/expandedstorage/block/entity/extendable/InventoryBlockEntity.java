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
import net.minecraft.inventory.Inventory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public abstract class InventoryBlockEntity extends OpenableBlockEntity {
    public InventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId) {
        super(type, pos, state, blockId);
    }

    public abstract Inventory getInventory();

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
}

