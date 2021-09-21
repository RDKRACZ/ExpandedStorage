package ninjaphenix.expandedstorage.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import ninjaphenix.expandedstorage.BarrelCommon;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.internal_api.block.AbstractOpenableStorageBlock;

import java.util.Random;

public final class BarrelBlock extends AbstractOpenableStorageBlock implements InventoryProvider {
    public BarrelBlock(AbstractBlock.Settings properties, Identifier blockId, Identifier blockTier, Identifier openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
        this.setDefaultState(this.getStateManager().getDefaultState().with(Properties.FACING, Direction.NORTH).with(Properties.OPEN, false));

    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING);
        builder.add(Properties.OPEN);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(Properties.FACING, context.getPlayerLookDirection().getOpposite());
    }

    @Override
    public Identifier getBlockType() {
        return BarrelCommon.BLOCK_TYPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(BarrelCommon.getBlockEntityType(), pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void scheduledTick(BlockState state, ServerWorld level, BlockPos pos, Random random) {
        if (level.getBlockEntity(pos) instanceof BarrelBlockEntity entity) {
            entity.recountObservers();
        }
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BarrelBlockEntity entity) {
            return entity.getContainerWrapper();
        }
        return null;
    }
}
