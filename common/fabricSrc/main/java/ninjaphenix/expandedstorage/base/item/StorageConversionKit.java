package ninjaphenix.expandedstorage.base.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.base.internal_api.BaseApi;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.internal_api.item.BlockUpgradeBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class StorageConversionKit extends Item {
    private final Identifier from;
    private final Identifier to;
    private final Text instructionsFirst;
    private final Text instructionsSecond;

    public StorageConversionKit(Settings properties, Identifier from, Identifier to) {
        super(properties);
        this.from = from;
        this.to = to;
        this.instructionsFirst = Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_1", Utils.ALT_USE)
                                      .formatted(Formatting.GRAY);
        this.instructionsSecond = Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_2", Utils.ALT_USE)
                                       .formatted(Formatting.GRAY);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World level = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (player != null && player.isSneaking()) {
            Block block = level.getBlockState(context.getBlockPos()).getBlock();
            Optional<BlockUpgradeBehaviour> maybeBehaviour = BaseApi.getInstance().getBlockUpgradeBehaviour(block);
            if (maybeBehaviour.isPresent()) {
                if (level.isClient()) {
                    return ActionResult.CONSUME;
                } else if (maybeBehaviour.get().tryUpgradeBlock(context, from, to)) {
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> list, TooltipContext flag) {
        list.add(instructionsFirst);
        if (!instructionsSecond.getString().equals("")) {
            list.add(instructionsSecond);
        }
    }
}
