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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import ninjaphenix.expandedstorage.block.ChestBlock;

public final class ChestBlockEntity extends AbstractChestBlockEntity {
    private final ChestLidController lidController;

    public ChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state, ((ChestBlock) state.getBlock()).getBlockId(), true);
        lidController = new ChestLidController();
    }

    public static void progressLidAnimation(Level world, BlockPos pos, BlockState state, ChestBlockEntity blockEntity) {
        blockEntity.lidController.tickLid();
    }

    private static void playSound(Level world, BlockPos pos, BlockState state, SoundEvent sound) {
        DoubleBlockCombiner.BlockType mergeType = ChestBlock.getBlockType(state);
        Vec3 soundPos;
        if (mergeType == DoubleBlockCombiner.BlockType.SINGLE) {
            soundPos = Vec3.atCenterOf(pos);
        } else if (mergeType == DoubleBlockCombiner.BlockType.FIRST) {
            soundPos = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(ChestBlock.getDirectionToAttached(state).getNormal()).scale(0.5D));
        } else {
            return;
        }
        world.playSound(null, soundPos.x(), soundPos.y(), soundPos.z(), sound, SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected void onOpen(Level world, BlockPos pos, BlockState state) {
        ChestBlockEntity.playSound(world, pos, state, SoundEvents.CHEST_OPEN);
    }

    @Override
    protected void onClose(Level world, BlockPos pos, BlockState state) {
        ChestBlockEntity.playSound(world, pos, state, SoundEvents.CHEST_CLOSE);
    }

    @Override
    protected void onObserverCountChanged(Level world, BlockPos pos, BlockState state, int oldCount, int newCount) {
        world.blockEvent(pos, state.getBlock(), ChestBlock.SET_OBSERVER_COUNT_EVENT, newCount);
    }

    @Override
    public boolean triggerEvent(int event, int value) {
        if (event == ChestBlock.SET_OBSERVER_COUNT_EVENT) {
            lidController.shouldBeOpen(value > 0);
            return true;
        }
        return super.triggerEvent(event, value);
    }

    // Client only
    public float getLidOpenness(float f) {
        return lidController.getOpenness(f);
    }
}
