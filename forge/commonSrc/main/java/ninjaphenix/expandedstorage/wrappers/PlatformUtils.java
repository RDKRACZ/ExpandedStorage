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

import ninjaphenix.expandedstorage.block.misc.AbstractAccessibleStorageBlockEntity;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PlatformUtils {
    private static PlatformUtils INSTANCE;
    private final boolean isClient;
    private final boolean isForge;

    protected PlatformUtils(boolean isClient, boolean isForge) {
        this.isClient = isClient;
        this.isForge = isForge;
    }

    public final boolean isClient() {
        return isClient;
    }

    public final boolean isForge() {
        return isForge;
    }

    public static PlatformUtils getInstance() {
        if (PlatformUtils.INSTANCE == null) {
            PlatformUtils.INSTANCE = new PlatformUtilsImpl();
        }
        return PlatformUtils.INSTANCE;
    }

    public abstract CreativeModeTab createTab(Supplier<ItemStack> icon);

    public abstract Object createGenericItemAccess(AbstractAccessibleStorageBlockEntity entity);

    public abstract Object createChestItemAccess(Level world, BlockState state, BlockPos pos, Direction side);
}
