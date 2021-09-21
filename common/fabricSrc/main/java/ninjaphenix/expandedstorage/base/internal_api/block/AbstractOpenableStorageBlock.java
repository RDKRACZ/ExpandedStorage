package ninjaphenix.expandedstorage.base.internal_api.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
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
import ninjaphenix.container_library.api.OpenableBlockEntityProvider;
import ninjaphenix.container_library.api.client.NCL_ClientApi;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Internal
@Experimental
public abstract class AbstractOpenableStorageBlock extends AbstractStorageBlock implements BlockEntityProvider, OpenableBlockEntityProvider {
    private final Identifier openingStat;
    private final int slots;

    public AbstractOpenableStorageBlock(AbstractBlock.Settings properties, Identifier blockId, Identifier blockTier,
                                        Identifier openingStat, int slots) {
        super(properties, blockId, blockTier);
        this.openingStat = openingStat;
        this.slots = slots;
    }

    public final int getSlotCount() {
        return slots;
    }

    public final Text getMenuTitle() {
        return new TranslatableText(this.getTranslationKey());
    }

    @Override
    @SuppressWarnings("deprecation")
    public final ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (level.isClient()) {
            NCL_ClientApi.openInventoryAt(pos);
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView getter, List<Text> tooltip, TooltipContext flag) {
        super.appendTooltip(stack, getter, tooltip, flag);
        tooltip.add(new TranslatableText("tooltip.expandedstorage.stores_x_stacks", slots).formatted(Formatting.GRAY));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World level, BlockPos pos, BlockState newState, boolean bl) {
        if (!state.isOf(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AbstractOpenableStorageBlockEntity entity) {
                ItemScatterer.spawn(level, pos, entity.getItems());
                level.updateComparators(pos, this);
            }
            super.onStateReplaced(state, level, pos, newState, bl);
        }
    }

    public void onInitialOpen(ServerPlayerEntity player) {
        player.incrementStat(openingStat);
    }
}
