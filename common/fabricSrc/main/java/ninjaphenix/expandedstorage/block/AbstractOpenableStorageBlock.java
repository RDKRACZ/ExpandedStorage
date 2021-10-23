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
package ninjaphenix.expandedstorage.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import ninjaphenix.container_library.api.v2.OpenableBlockEntityProviderV2;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Internal
@Experimental
public abstract class AbstractOpenableStorageBlock extends AbstractStorageBlock implements BlockEntityProvider, OpenableBlockEntityProviderV2 {
    private final Identifier openingStat;
    private final int slots;

    public AbstractOpenableStorageBlock(AbstractBlock.Settings settings, Identifier blockId, Identifier blockTier,
                                        Identifier openingStat, int slots) {
        super(settings, blockId, blockTier);
        this.openingStat = openingStat;
        this.slots = slots;
    }

    public final int getSlotCount() {
        return slots;
    }

    public final Text getInventoryTitle() {
        return new TranslatableText(this.getTranslationKey());
    }

    @Override
    @SuppressWarnings("deprecation")
    public final ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return this.ncl_onBlockUse(world, state, pos, player, hand, hit);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView view, List<Text> tooltip, TooltipContext flag) {
        super.appendTooltip(stack, view, tooltip, flag);
        tooltip.add(new TranslatableText("tooltip.expandedstorage.stores_x_stacks", slots).formatted(Formatting.GRAY));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean bl) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof AbstractOpenableStorageBlockEntity entity) {
                ItemScatterer.spawn(world, pos, entity.getItems());
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, bl);
        }
    }

    public void onInitialOpen(ServerPlayerEntity player) {
        player.incrementStat(openingStat);
        PiglinBrain.onGuardedBlockInteracted(player, true);
    }

    // todo: move, should check for AbstractNameableAccessibleStorageBlockEntity
    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof AbstractOpenableStorageBlockEntity entity && stack.hasCustomName()) {
            entity.setTitle(stack.getName());
        }
    }
}
