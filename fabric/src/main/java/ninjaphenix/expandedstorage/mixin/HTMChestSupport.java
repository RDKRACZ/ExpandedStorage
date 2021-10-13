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
package ninjaphenix.expandedstorage.mixin;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.FabricChestProperties;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

// todo: find alternative for this, mixins on mod classes aren't allowed but work.
@Mixin(AbstractChestBlock.class)
public abstract class HTMChestSupport implements LockableChestBlock {
    @Override
    public HTMContainerLock getLockAt(BlockState state, World level, BlockPos pos) {
        return AbstractChestBlock.createPropertyRetriever(self(), state, level, pos, true).get(FabricChestProperties.LOCK_PROPERTY).orElse(null);
    }

    // Seems to be used to synchronize lock between both parts of chest.
    @Override
    public Optional<BlockEntity> getUnlockedPart(BlockState state, World level, BlockPos pos) {
        return AbstractChestBlock.createPropertyRetriever(self(), state, level, pos, true).get(FabricChestProperties.UNLOCKED_BE_PROPERTY);
    }

    private AbstractChestBlock<AbstractOpenableStorageBlockEntity> self() {
        //noinspection ConstantConditions, unchecked
        return (AbstractChestBlock<AbstractOpenableStorageBlockEntity>) (Object) this;
    }
}
