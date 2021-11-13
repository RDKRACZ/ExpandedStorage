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
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ninjaphenix.container_library.api.v2.OpenableBlockEntityV2;

public abstract class OpenableBlockEntity extends StrategyBlockEntity implements OpenableBlockEntityV2 {
    public OpenableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId) {
        super(type, pos, state, blockId);
    }

    @Override
    public boolean canBeUsedBy(ServerPlayerEntity player) {
        //noinspection ConstantConditions
        return this.getWorld().getBlockEntity(this.getPos()) == this &&
                player.squaredDistanceTo(Vec3d.ofCenter(this.getPos())) <= 64.0D &&
                this.getLockable().canPlayerOpenLock(player);
    }

    @Override
    public Text getInventoryTitle() {
        return this.getNameable().get();
    }

    public abstract DefaultedList<ItemStack> getItems();
}
