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

import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BarrelBlock extends AbstractOpenableStorageBlock implements WorldlyContainerHolder {
    public BarrelBlock(BlockBehaviour.Properties properties, ResourceLocation blockId, ResourceLocation blockTier, ResourceLocation openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
        this.registerDefaultState(this.getStateDefinition().any().setValue(BlockStateProperties.FACING, Direction.NORTH).setValue(BlockStateProperties.OPEN, false));

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
        builder.add(BlockStateProperties.OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public ResourceLocation getBlockType() {
        return Common.BARREL_BLOCK_TYPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(Common.getBarrelBlockEntityType(), pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof BarrelBlockEntity entity) {
            entity.recountObservers();
        }
    }

    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof BarrelBlockEntity entity) {
            return entity.getInventory();
        }
        return null;
    }
}
