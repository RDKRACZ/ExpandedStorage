package ninjaphenix.expandedstorage.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface MutatorBehaviour {
    ActionResult attempt(ItemUsageContext context, World world, BlockState state, BlockPos pos, ItemStack stack);
}
