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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.block.misc.strategies.temp_be.BarrelBlockEntity;

import java.util.Random;

public final class BarrelBlock extends AbstractOpenableStorageBlock {
    public BarrelBlock(AbstractBlock.Settings properties, Identifier blockId, Identifier blockTier, Identifier openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
        this.setDefaultState(this.getStateManager().getDefaultState().with(Properties.FACING, Direction.NORTH).with(Properties.OPEN, false));

    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING);
        builder.add(Properties.OPEN);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(Properties.FACING, context.getPlayerLookDirection().getOpposite());
    }

    @Override
    public Identifier getBlockType() {
        return Common.BARREL_BLOCK_TYPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(Common.getBarrelBlockEntityType(), pos, state, null, null, null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof BarrelBlockEntity entity) {
            //entity.recountObservers();
        }
    }

    // todo: re-add inventory provider, not sure why I removed it.
}
