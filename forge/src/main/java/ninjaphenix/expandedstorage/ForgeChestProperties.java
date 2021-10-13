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
package ninjaphenix.expandedstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.block.misc.Property;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

public final class ForgeChestProperties {
    public static final Property<AbstractOpenableStorageBlockEntity, Object> INVENTORY_GETTER = new Property<>() {
        @Override
        public Object get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            return new CombinedInvWrapper(
                    (IItemHandlerModifiable) PlatformUtils.getInstance().createGenericItemAccess(first),
                    (IItemHandlerModifiable) PlatformUtils.getInstance().createGenericItemAccess(second)
            );
        }

        @Override
        public Object get(AbstractOpenableStorageBlockEntity single) {
            return PlatformUtils.getInstance().createGenericItemAccess(single);
        }
    };

    public static Object createItemHandler(Level level, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            //noinspection unchecked
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, level, pos, false).get(ForgeChestProperties.INVENTORY_GETTER).orElse(null);
        }
        return null;
    }
}
