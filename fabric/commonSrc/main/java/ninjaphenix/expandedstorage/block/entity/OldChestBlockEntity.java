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
package ninjaphenix.expandedstorage.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.OpenableBlock;
import ninjaphenix.expandedstorage.block.entity.extendable.InventoryBlockEntity;
import ninjaphenix.expandedstorage.block.entity.extendable.OpenableBlockEntity;
import ninjaphenix.expandedstorage.block.strategies.ItemAccess;
import ninjaphenix.expandedstorage.block.strategies.Lockable;
import ninjaphenix.expandedstorage.block.misc.MutableNameable;

import java.util.function.Function;

public class OldChestBlockEntity extends InventoryBlockEntity {
    public OldChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId,
                               Function<OpenableBlockEntity, ItemAccess> access, Function<OpenableBlockEntity, Lockable> lockable) {
        super(type, pos, state, blockId, ((AbstractChestBlock) state.getBlock()).getSlotCount());
        this.setItemAccess(access.apply(this));
        this.setLockable(lockable.apply(this));
        this.setNameable(new MutableNameable(((OpenableBlock) state.getBlock()).getInventoryTitle()));
    }

    @Override
    protected boolean shouldStateUpdateInvalidateItemAccess(BlockState oldState, BlockState newState) {
        return oldState.get(AbstractChestBlock.CURSED_CHEST_TYPE) != newState.get(AbstractChestBlock.CURSED_CHEST_TYPE);
    }
}
