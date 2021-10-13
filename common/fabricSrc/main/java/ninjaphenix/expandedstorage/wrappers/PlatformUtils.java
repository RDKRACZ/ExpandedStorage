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

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;

import java.util.function.Supplier;

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

    public abstract ItemGroup createTab(Supplier<ItemStack> icon);

    public abstract Object createGenericItemAccess(AbstractOpenableStorageBlockEntity abstractOpenableStorageBlockEntity);

    public abstract Object createChestItemAccess(World world, BlockState state, BlockPos pos, Direction side);
}
