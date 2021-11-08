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
package ninjaphenix.expandedstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ninjaphenix.container_library.api.v2.OpenableBlockEntityProviderV2;

public abstract class OpenableBlock extends Block implements OpenableBlockEntityProviderV2, BlockEntityProvider {
    private final Identifier blockId;
    private final Identifier blockTier;
    private final Identifier openingStat;
    private final int slotCount;

    public OpenableBlock(Settings settings, Identifier blockId, Identifier blockTier, Identifier openingStat, int slotCount) {
        super(settings);
        this.blockId = blockId;
        this.blockTier = blockTier;
        this.openingStat = openingStat;
        this.slotCount = slotCount;
    }

    public Text getInventoryTitle() {
        return this.getName();
    }

    public abstract Identifier getBlockType();

    public final Identifier getBlockId() {
        return blockId;
    }

    public final int getSlotCount() {
        return slotCount;
    }

    public final Identifier getBlockTier() {
        return blockTier;
    }

    @Override
    public void onInitialOpen(ServerPlayerEntity player) {
        player.incrementStat(openingStat);
    }
}
