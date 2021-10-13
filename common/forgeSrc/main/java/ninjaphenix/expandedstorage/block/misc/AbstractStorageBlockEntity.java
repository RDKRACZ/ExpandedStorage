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
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public abstract class AbstractStorageBlockEntity extends BlockEntity {
    private LockCode lockKey;
    private Component title;

    public AbstractStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        lockKey = LockCode.NO_LOCK;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lockKey = LockCode.fromTag(tag);
        if (tag.contains("CustomName", Tag.TAG_STRING)) {
            title = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        lockKey.addToTag(tag);
        if (title != null) {
            tag.putString("CustomName", Component.Serializer.toJson(title));
        }
        return tag;
    }

    public boolean usableBy(ServerPlayer player) {
        return lockKey == LockCode.NO_LOCK || !player.isSpectator() && lockKey.unlocksWith(player.getMainHandItem());
    }

    public final Component getTitle() {
        return this.hasCustomTitle() ? title : this.getDefaultTitle();
    }

    protected abstract Component getDefaultTitle();

    public final boolean hasCustomTitle() {
        return title != null;
    }

    public final void setTitle(Component title) {
        this.title = title;
    }
}
