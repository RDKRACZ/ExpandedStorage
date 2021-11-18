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
package ninjaphenix.expandedstorage.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class StorageConversionKit extends Item {
    private final Identifier from;
    private final Identifier to;
    private final Text instructionsFirst;
    private final Text instructionsSecond;

    public StorageConversionKit(Settings settings, Identifier from, Identifier to, boolean manuallyWrapTooltips) {
        super(settings);
        this.from = from;
        this.to = to;
        if (manuallyWrapTooltips) {
            this.instructionsFirst = Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_1", Utils.ALT_USE).formatted(Formatting.GRAY);
            this.instructionsSecond = Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_2", Utils.ALT_USE).formatted(Formatting.GRAY);
        } else {
            this.instructionsFirst = Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_1", Utils.ALT_USE).formatted(Formatting.GRAY).append(Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_2", Utils.ALT_USE).formatted(Formatting.GRAY));
            this.instructionsSecond = new LiteralText("");
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (player != null) {
            if (player.isSneaking()) {
                Block block = world.getBlockState(context.getBlockPos()).getBlock();
                BlockUpgradeBehaviour behaviour = Common.getBlockUpgradeBehaviour(block);
                if (behaviour != null) {
                    if (world.isClient()) {
                        return ActionResult.CONSUME;
                    } else if (behaviour.tryUpgradeBlock(context, from, to)) {
                        return ActionResult.SUCCESS;
                    }
                    player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                }
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext context) {
        list.add(instructionsFirst);
        if (!instructionsSecond.getString().equals("")) {
            list.add(instructionsSecond);
        }
    }
}
