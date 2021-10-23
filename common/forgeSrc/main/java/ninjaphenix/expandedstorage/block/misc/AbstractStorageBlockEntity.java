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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.expandedstorage.block.AbstractStorageBlock;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public abstract class AbstractStorageBlockEntity<T extends AbstractStorageBlock> extends BlockEntity {
    private final ResourceLocation blockId;
    private LockCode lockKey;

    public AbstractStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, ResourceLocation blockId) {
        super(blockEntityType, pos, state);
        lockKey = LockCode.NO_LOCK;
        this.blockId = blockId;
        this.initialise(blockId, (T) state.getBlock());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lockKey = LockCode.fromTag(tag);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        lockKey.addToTag(tag);
        return tag;
    }

    public boolean usableBy(ServerPlayer player) {
        return lockKey == LockCode.NO_LOCK || !player.isSpectator() && lockKey.unlocksWith(player.getMainHandItem());
    }

    public final ResourceLocation getBlockId() {
        return blockId;
    }

    protected abstract void initialise(ResourceLocation blockId, T block);
}
