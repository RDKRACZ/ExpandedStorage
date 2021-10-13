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
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestLidAnimator;
import net.minecraft.inventory.Inventory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ninjaphenix.container_library.api.helpers.VariableInventory;
import ninjaphenix.expandedstorage.block.ChestBlock;

public final class ChestBlockEntity extends AbstractChestBlockEntity {
    private final ChestLidAnimator lidController;

    public ChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state, ((ChestBlock) state.getBlock()).getBlockId());
        lidController = new ChestLidAnimator();
    }

    public static void progressLidAnimation(World world, BlockPos pos, BlockState state, ChestBlockEntity blockEntity) {
        blockEntity.lidController.step();
    }

    private static void playSound(World world, BlockPos pos, BlockState state, SoundEvent sound) {
        DoubleBlockProperties.Type mergeType = ChestBlock.getBlockType(state);
        Vec3d soundPos;
        if (mergeType == DoubleBlockProperties.Type.SINGLE) {
            soundPos = Vec3d.ofCenter(pos);
        } else if (mergeType == DoubleBlockProperties.Type.FIRST) {
            soundPos = Vec3d.ofCenter(pos).add(Vec3d.of(ChestBlock.getDirectionToAttached(state).getVector()).multiply(0.5D));
        } else {
            return;
        }
        world.playSound(null, soundPos.getX(), soundPos.getY(), soundPos.getZ(), sound, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected void onOpen(World world, BlockPos pos, BlockState state) {
        ChestBlockEntity.playSound(world, pos, state, SoundEvents.BLOCK_CHEST_OPEN);
    }

    @Override
    protected void onClose(World world, BlockPos pos, BlockState state) {
        ChestBlockEntity.playSound(world, pos, state, SoundEvents.BLOCK_CHEST_CLOSE);
    }

    @Override
    protected void onObserverCountChanged(World world, BlockPos pos, BlockState state, int oldCount, int newCount) {
        world.addSyncedBlockEvent(pos, state.getBlock(), ChestBlock.SET_OBSERVER_COUNT_EVENT, newCount);
    }

    @Override
    protected boolean isThis(Inventory inventory) {
        return super.isThis(inventory) || inventory instanceof VariableInventory variableInventory && variableInventory.containsPart(this.getInventory());
    }

    @Override
    public boolean onSyncedBlockEvent(int event, int value) {
        if (event == ChestBlock.SET_OBSERVER_COUNT_EVENT) {
            lidController.setOpen(value > 0);
            return true;
        }
        return super.onSyncedBlockEvent(event, value);
    }

    // Client only
    public float getLidOpenness(float f) {
        return lidController.getProgress(f);
    }
}
