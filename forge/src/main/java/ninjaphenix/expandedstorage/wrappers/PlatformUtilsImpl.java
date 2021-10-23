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
package ninjaphenix.expandedstorage.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.items.wrapper.InvWrapper;
import ninjaphenix.expandedstorage.ForgeChestProperties;
import ninjaphenix.expandedstorage.Utils;
import ninjaphenix.expandedstorage.block.misc.AbstractAccessibleStorageBlockEntity;

import java.util.function.Supplier;

final class PlatformUtilsImpl extends PlatformUtils {
    PlatformUtilsImpl() {
        super(FMLLoader.getDist() == Dist.CLIENT, true);
    }

    @Override
    public CreativeModeTab createTab(Supplier<ItemStack> icon) {
        return new CreativeModeTab(Utils.MOD_ID) {
            @Override
            public ItemStack makeIcon() {
                return icon.get();
            }
        };
    }

    @Override
    public Object createGenericItemAccess(AbstractAccessibleStorageBlockEntity entity) {
        return new InvWrapper(entity.getInventory());
    }

    @Override
    public Object createChestItemAccess(Level world, BlockState state, BlockPos pos, Direction side) {
        return ForgeChestProperties.createItemHandler(world, state, pos);
    }
}
