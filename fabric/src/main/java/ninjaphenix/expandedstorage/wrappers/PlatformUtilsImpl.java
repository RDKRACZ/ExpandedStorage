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

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.FabricChestProperties;
import ninjaphenix.expandedstorage.Utils;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;

import java.util.function.Supplier;

public final class PlatformUtilsImpl extends PlatformUtils {
    PlatformUtilsImpl() {
        super(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT, false);
    }

    @Override
    public ItemGroup createTab(Supplier<ItemStack> icon) { // Hopefully fabric api gets rid of this builder in favour of transitive AW.
        FabricItemGroupBuilder.build(new Identifier("dummy"), null); // Fabric API is dumb.
        return new ItemGroup(ItemGroup.GROUPS.length - 1, Utils.MOD_ID) {
            @Override
            public ItemStack createIcon() {
                return icon.get();
            }
        };
    }

    @Override
    public Object createGenericItemAccess(AbstractOpenableStorageBlockEntity entity) {
        //noinspection UnstableApiUsage,deprecation
        return InventoryStorage.of(entity.getInventory(), null);
    }

    @Override
    public Object createChestItemAccess(World world, BlockState state, BlockPos pos, Direction side) {
        return FabricChestProperties.createItemStorage(world, state, pos);
    }
}
