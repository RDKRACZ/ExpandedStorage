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
package ninjaphenix.expandedstorage.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.block.misc.AbstractChestBlockEntity;

public final class OldChestBlock extends AbstractChestBlock<AbstractChestBlockEntity> {
    public OldChestBlock(Settings properties, Identifier blockId, Identifier blockTier, Identifier openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
    }

    @Override
    protected BlockEntityType<AbstractChestBlockEntity> getBlockEntityType() {
        return Common.getOldChestBlockEntityType();
    }

    @Override
    public Identifier getBlockType() {
        return Common.OLD_CHEST_BLOCK_TYPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AbstractChestBlockEntity(Common.getOldChestBlockEntityType(), pos, state, this.getBlockId());
    }
}
