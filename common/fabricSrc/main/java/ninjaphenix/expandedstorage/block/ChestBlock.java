package ninjaphenix.expandedstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;

public class ChestBlock extends AbstractChestBlock implements Waterloggable {
    private static final VoxelShape[] SHAPES = {
            Block.createCuboidShape(1, 0, 0, 15, 14, 15), // Horizontal shapes, depends on orientation and chest type.
            Block.createCuboidShape(1, 0, 1, 16, 14, 15),
            Block.createCuboidShape(1, 0, 1, 15, 14, 16),
            Block.createCuboidShape(0, 0, 1, 15, 14, 15),
            Block.createCuboidShape(1, 0, 1, 15, 14, 15), // Top shape.
            Block.createCuboidShape(1, 0, 1, 15, 16, 15), // Bottom shape.
            Block.createCuboidShape(1, 0, 1, 15, 14, 15)  // Single shape.
    };

    public ChestBlock(Settings settings, Identifier blockId, Identifier blockTier, Identifier openingStat, int slotCount) {
        super(settings, blockId, blockTier, openingStat, slotCount);
        this.setDefaultState(this.getDefaultState().with(Properties.WATERLOGGED, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        CursedChestType type = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (type == CursedChestType.TOP) {
            return ChestBlock.SHAPES[4];
        } else if (type == CursedChestType.BOTTOM) {
            return ChestBlock.SHAPES[5];
        } else if (type == CursedChestType.SINGLE) {
            return ChestBlock.SHAPES[6];
        } else {
            int index = (state.get(Properties.HORIZONTAL_FACING).getHorizontal() + type.getOffset()) % 4;
            return ChestBlock.SHAPES[index];
        }
    }

    @Override
    protected void appendAdditionalStateDefinitions(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        return super.getPlacementState(context).with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState otherState, WorldAccess world, BlockPos pos, BlockPos otherPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, otherState, world, pos, otherPos);
    }


    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Common.createChestBlockEntity(pos, state);
    }
}
