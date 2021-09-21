package ninjaphenix.expandedstorage.base.internal_api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import ninjaphenix.container_library.api.OpenableBlockEntity;
import ninjaphenix.container_library.api.helpers.OpenableBlockEntities;
import ninjaphenix.container_library.api.helpers.VariableSidedInventory;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.FaceRotation;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.Property;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.PropertyRetriever;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

@Internal
@Experimental
public abstract class AbstractChestBlock<T extends AbstractOpenableStorageBlockEntity> extends AbstractOpenableStorageBlock implements InventoryProvider {
    public static final EnumProperty<CursedChestType> CURSED_CHEST_TYPE = EnumProperty.of("type", CursedChestType.class);
    public static final EnumProperty<FaceRotation> FACE_ROTATION = EnumProperty.of("face_rotation", FaceRotation.class);
    public static final EnumProperty<FaceRotation> PERP_ROTATION = EnumProperty.of("perp_rotation", FaceRotation.class);
    public static final EnumProperty<FaceRotation> Y_ROTATION = DirectionProperty.of("facing", FaceRotation.class);

    public AbstractChestBlock(Settings properties, Identifier blockId, Identifier blockTier, Identifier openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
        this.setDefaultState(this.getStateManager().getDefaultState()
                                 .with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE)
                                 .with(AbstractChestBlock.PERP_ROTATION, FaceRotation.NORTH)
                                 .with(AbstractChestBlock.FACE_ROTATION, FaceRotation.NORTH)
                                 .with(AbstractChestBlock.Y_ROTATION, FaceRotation.NORTH));
    }

    public static Direction getDirectionToAttached(BlockState state) {
        CursedChestType value = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (value == CursedChestType.SINGLE) {
            throw new IllegalArgumentException("BaseChestBlock#getDirectionToAttached received an unexpected state.");
        }
        FaceRotation face = state.get(AbstractChestBlock.FACE_ROTATION);
        FaceRotation y = state.get(AbstractChestBlock.Y_ROTATION);
        FaceRotation perpendicular = state.get(AbstractChestBlock.PERP_ROTATION);
        return switch (value) {
            case TOP -> FaceRotation.getRelativeDirection(Direction.UP, face, y, perpendicular);
            case BOTTOM -> FaceRotation.getRelativeDirection(Direction.DOWN, face, y, perpendicular);
            case FRONT -> FaceRotation.getRelativeDirection(Direction.SOUTH, face, y, perpendicular);
            case BACK -> FaceRotation.getRelativeDirection(Direction.NORTH, face, y, perpendicular);
            case LEFT -> FaceRotation.getRelativeDirection(Direction.EAST, face, y, perpendicular);
            case RIGHT -> FaceRotation.getRelativeDirection(Direction.WEST, face, y, perpendicular);
            default -> throw new IllegalStateException("Unreachable code.");
        };
    }

    public static DoubleBlockProperties.Type getBlockType(BlockState state) {
        CursedChestType value = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (value == CursedChestType.TOP || value == CursedChestType.LEFT || value == CursedChestType.FRONT) {
            return DoubleBlockProperties.Type.FIRST;
        } else if (value == CursedChestType.BACK || value == CursedChestType.RIGHT || value == CursedChestType.BOTTOM) {
            return DoubleBlockProperties.Type.SECOND;
        } else if (value == CursedChestType.SINGLE) {
            return DoubleBlockProperties.Type.SINGLE;
        }
        throw new IllegalArgumentException("Invalid CursedChestType passed.");
    }

    public static CursedChestType getChestType(FaceRotation yRotation, FaceRotation faceRotation, FaceRotation perpRotation, Direction offset) {
        Direction definitiveFacing = yRotation.asDirection(Direction.Axis.Y);
        Direction definitiveOffset = FaceRotation.getDefinitiveDirection(offset, faceRotation, yRotation, perpRotation);
        if (definitiveOffset == Direction.DOWN) {
            return CursedChestType.TOP;
        } else if (definitiveOffset == Direction.UP) {
            return CursedChestType.BOTTOM;
        } else if (definitiveFacing.rotateYClockwise() == definitiveOffset) {
            return CursedChestType.RIGHT;
        } else if (definitiveFacing.rotateYCounterclockwise() == definitiveOffset) {
            return CursedChestType.LEFT;
        } else if (definitiveFacing == definitiveOffset) {
            return CursedChestType.BACK;
        } else if (definitiveFacing.getOpposite() == definitiveOffset) {
            return CursedChestType.FRONT;
        }
        return CursedChestType.SINGLE;
    }

    @Override
    protected final void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AbstractChestBlock.CURSED_CHEST_TYPE);
        builder.add(AbstractChestBlock.PERP_ROTATION);
        builder.add(AbstractChestBlock.FACE_ROTATION);
        builder.add(AbstractChestBlock.Y_ROTATION);
        this.appendAdditionalStateDefinitions(builder);
    }

    @NotNull
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        CursedChestType chestType = CursedChestType.SINGLE;
        Direction direction_1 = context.getPlayerFacing().getOpposite();
        Direction direction_2 = context.getSide();
        if (context.shouldCancelInteraction()) {
            BlockState state;
            if (direction_2.getAxis().isVertical()) {
                state = level.getBlockState(pos.offset(direction_2.getOpposite()));
                if (state.getBlock() == this && state.get(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    Direction direction_3 = state.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y);
                    if (direction_3.getAxis() != direction_2.getAxis() && direction_3 == direction_1) {
                        chestType = direction_2 == Direction.UP ? CursedChestType.TOP : CursedChestType.BOTTOM;
                    }
                }
            } else {
                Direction offsetDir = direction_2.getOpposite();
                BlockState clickedBlock = level.getBlockState(pos.offset(offsetDir));
                if (clickedBlock.getBlock() == this && clickedBlock.get(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    if (clickedBlock.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y) == direction_2 && clickedBlock.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y) == direction_1) {
                        chestType = CursedChestType.FRONT;
                    } else {
                        state = level.getBlockState(pos.offset(direction_2.getOpposite()));
                        if (state.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y).getHorizontal() < 2) {
                            offsetDir = offsetDir.getOpposite();
                        }
                        if (direction_1 == state.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y)) {
                            chestType = (offsetDir == Direction.WEST || offsetDir == Direction.NORTH) ? CursedChestType.LEFT : CursedChestType.RIGHT;
                        }
                    }
                }
            }
        } else {
            for (Direction dir : Direction.values()) {
                BlockState state = level.getBlockState(pos.offset(dir));
                if (state.getBlock() != this || state.get(AbstractChestBlock.CURSED_CHEST_TYPE) != CursedChestType.SINGLE || state.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y) != direction_1) {
                    continue;
                }
                CursedChestType type = AbstractChestBlock.getChestType(FaceRotation.of(direction_1), state.get(AbstractChestBlock.FACE_ROTATION), state.get(AbstractChestBlock.PERP_ROTATION), dir);
                if (type != CursedChestType.SINGLE) {
                    chestType = type;
                    break;
                }
            }
        }
        return this.getDefaultState().with(AbstractChestBlock.Y_ROTATION, FaceRotation.of(direction_1)).with(AbstractChestBlock.CURSED_CHEST_TYPE, chestType);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction offset, BlockState offsetState, WorldAccess level,
                                                BlockPos pos, BlockPos offsetPos) {
        DoubleBlockProperties.Type mergeType = AbstractChestBlock.getBlockType(state);
        if (mergeType == DoubleBlockProperties.Type.SINGLE) {
            Direction facing = state.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y);
            if (!offsetState.contains(AbstractChestBlock.CURSED_CHEST_TYPE)) {
                return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
            }
            CursedChestType newType = AbstractChestBlock.getChestType(FaceRotation.of(facing), state.get(AbstractChestBlock.FACE_ROTATION), state.get(AbstractChestBlock.PERP_ROTATION), offset);
            if (offsetState.get(AbstractChestBlock.CURSED_CHEST_TYPE) == newType.getOpposite() && facing == offsetState.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y)) {
                return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, newType);
            }
        } else if (level.getBlockState(pos.offset(AbstractChestBlock.getDirectionToAttached(state))).getBlock() != this) {
            return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
        }
        return super.getStateForNeighborUpdate(state, offset, offsetState, level, pos, offsetPos);
    }

    protected void appendAdditionalStateDefinitions(StateManager.Builder<Block, BlockState> builder) {

    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(AbstractChestBlock.Y_ROTATION, state.get(AbstractChestBlock.Y_ROTATION).rotated(rotation));
    }

    public static <A extends AbstractOpenableStorageBlockEntity> PropertyRetriever<A> createPropertyRetriever(AbstractChestBlock<A> block, BlockState state, WorldAccess level, BlockPos pos, boolean alwaysOpen) {
        //BiPredicate<LevelAccessor, BlockPos> isChestBlocked = alwaysOpen ? (_level, _pos) -> false : block::isAccessBlocked;
        //return DoubleBlockCombiner.combineWithNeigbour(block.getBlockEntityType(), AbstractChestBlock::getBlockType,
        //        AbstractChestBlock::getDirectionToAttached, AbstractChestBlock.Y_ROTATION, state, level, pos, isChestBlocked);
        // todo: reimplement
        return PropertyRetriever.create(level, state, pos);
    }

    protected abstract BlockEntityType<T> getBlockEntityType();

    protected boolean isAccessBlocked(WorldAccess level, BlockPos pos) {
        return false;
    }

    @Override
    public OpenableBlockEntity getOpenableBlockEntity(World level, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, level, pos, false).get(new Property<>() {
                @Override
                public OpenableBlockEntity get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
                    Text name = first.hasCustomName() ? first.getDisplayName() : second.hasCustomName() ? second.getDisplayName() : Utils.translation("container.expandedstorage.generic_double", first.getDisplayName());
                    return new OpenableBlockEntities(name, first, second);
                }

                @Override
                public OpenableBlockEntity get(AbstractOpenableStorageBlockEntity single) {
                    return single;
                }
            });
        }
        return null;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess level, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, level, pos, false).get(new Property<>() {
                @Override
                public SidedInventory get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
                    return VariableSidedInventory.of(first.getContainerWrapper(), second.getContainerWrapper());
                }

                @Override
                public SidedInventory get(AbstractOpenableStorageBlockEntity single) {
                    return single.getContainerWrapper();
                }
            });
        }
        return null;
    }
}
