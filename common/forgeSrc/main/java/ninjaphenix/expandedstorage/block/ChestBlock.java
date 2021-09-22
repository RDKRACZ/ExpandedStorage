package ninjaphenix.expandedstorage.block;

import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.block.misc.FaceRotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ChestBlock extends AbstractChestBlock<ChestBlockEntity> implements SimpleWaterloggedBlock {
    public static final int SET_OBSERVER_COUNT_EVENT = 1;
    private static final VoxelShape[] SHAPES = {
            Block.box(1, 0, 0, 15, 14, 15), // Horizontal shapes, depends on orientation and chest type.
            Block.box(1, 0, 1, 16, 14, 15),
            Block.box(1, 0, 1, 15, 14, 16),
            Block.box(0, 0, 1, 15, 14, 15),
            Block.box(1, 0, 1, 15, 14, 15), // Top shape.
            Block.box(1, 0, 1, 15, 16, 15), // Bottom shape.
            Block.box(1, 0, 1, 15, 14, 15)  // Single shape.
    };

    public ChestBlock(Properties properties, ResourceLocation blockId, ResourceLocation blockTier,
                      ResourceLocation openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor world, BlockPos pos, BlockPos otherPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, direction, otherState, world, pos, otherPos);
    }

    @Override
    public ResourceLocation getBlockType() {
        return Common.CHEST_BLOCK_TYPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> blockEntityType) {
        boolean correctBET = blockEntityType == Common.getChestBlockEntityType();
        return world.isClientSide() && correctBET ? (world1, pos, state1, entity) -> ChestBlockEntity.progressLidAnimation(world1, pos, state1, (ChestBlockEntity) entity) : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof ChestBlockEntity entity) {
            entity.recountObservers();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        CursedChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (type == CursedChestType.TOP) {
            return ChestBlock.SHAPES[4];
        } else if (type == CursedChestType.BOTTOM) {
            return ChestBlock.SHAPES[5];
        } else if (type == CursedChestType.SINGLE) {
            return ChestBlock.SHAPES[6];
        } else {
            int index = (state.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y).get2DDataValue() + type.getOffset()) % 4;
            return ChestBlock.SHAPES[index];
        }
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChestBlockEntity(Common.getChestBlockEntityType(), pos, state);
    }

    @Override
    protected void appendAdditionalStateDefinitions(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected BlockEntityType<ChestBlockEntity> getBlockEntityType() {
        return Common.getChestBlockEntityType();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int event, int value) {
        super.triggerEvent(state, world, pos, event, value);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(event, value);
    }

    @Override
    protected boolean isAccessBlocked(LevelAccessor world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockPos abovePos = pos.relative(FaceRotation.getRelativeDirection(Direction.UP, state.getValue(FACE_ROTATION), state.getValue(Y_ROTATION), state.getValue(PERP_ROTATION)));
        return ChestBlock.isSolidBlockAt(world, abovePos) || ChestBlock.isCatSittingAt(world, pos, abovePos);
    }

    private static boolean isCatSittingAt(LevelAccessor world, BlockPos chestPos, BlockPos abovePos) {
        List<Cat> cats = world.getEntitiesOfClass(Cat.class, new AABB(chestPos, abovePos));
        for (Cat cat : cats) {
            if (cat.isInSittingPose()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSolidBlockAt(LevelAccessor world, BlockPos abovePos) {
        return world.getBlockState(abovePos).isRedstoneConductor(world, abovePos);
    }
}
