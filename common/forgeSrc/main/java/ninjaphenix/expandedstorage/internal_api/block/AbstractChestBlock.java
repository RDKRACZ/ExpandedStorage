package ninjaphenix.expandedstorage.internal_api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import ninjaphenix.container_library.api.OpenableBlockEntity;
import ninjaphenix.container_library.api.helpers.OpenableBlockEntities;
import ninjaphenix.container_library.api.helpers.VariableSidedInventory;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.internal_api.block.misc.FaceRotation;
import ninjaphenix.expandedstorage.internal_api.block.misc.Property;
import ninjaphenix.expandedstorage.internal_api.block.misc.PropertyRetriever;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

@Internal
@Experimental
public abstract class AbstractChestBlock<T extends AbstractOpenableStorageBlockEntity> extends AbstractOpenableStorageBlock implements WorldlyContainerHolder {
    public static final EnumProperty<CursedChestType> CURSED_CHEST_TYPE = EnumProperty.create("type", CursedChestType.class);
    public static final EnumProperty<FaceRotation> FACE_ROTATION = EnumProperty.create("face_rotation", FaceRotation.class);
    public static final EnumProperty<FaceRotation> PERP_ROTATION = EnumProperty.create("perp_rotation", FaceRotation.class);
    public static final EnumProperty<FaceRotation> Y_ROTATION = DirectionProperty.create("facing", FaceRotation.class);

    public AbstractChestBlock(Properties properties, ResourceLocation blockId, ResourceLocation blockTier, ResourceLocation openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
        this.registerDefaultState(this.getStateDefinition().any()
                                 .setValue(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE)
                                 .setValue(AbstractChestBlock.PERP_ROTATION, FaceRotation.NORTH)
                                 .setValue(AbstractChestBlock.FACE_ROTATION, FaceRotation.NORTH)
                                 .setValue(AbstractChestBlock.Y_ROTATION, FaceRotation.NORTH));
    }

    public static Direction getDirectionToAttached(BlockState state) {
        CursedChestType value = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (value == CursedChestType.SINGLE) {
            throw new IllegalArgumentException("BaseChestBlock#getDirectionToAttached received an unexpected state.");
        }
        FaceRotation face = state.getValue(AbstractChestBlock.FACE_ROTATION);
        FaceRotation y = state.getValue(AbstractChestBlock.Y_ROTATION);
        FaceRotation perpendicular = state.getValue(AbstractChestBlock.PERP_ROTATION);
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

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState state) {
        CursedChestType value = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (value == CursedChestType.TOP || value == CursedChestType.LEFT || value == CursedChestType.FRONT) {
            return DoubleBlockCombiner.BlockType.FIRST;
        } else if (value == CursedChestType.BACK || value == CursedChestType.RIGHT || value == CursedChestType.BOTTOM) {
            return DoubleBlockCombiner.BlockType.SECOND;
        } else if (value == CursedChestType.SINGLE) {
            return DoubleBlockCombiner.BlockType.SINGLE;
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
        } else if (definitiveFacing.getClockWise() == definitiveOffset) {
            return CursedChestType.RIGHT;
        } else if (definitiveFacing.getCounterClockWise() == definitiveOffset) {
            return CursedChestType.LEFT;
        } else if (definitiveFacing == definitiveOffset) {
            return CursedChestType.BACK;
        } else if (definitiveFacing.getOpposite() == definitiveOffset) {
            return CursedChestType.FRONT;
        }
        return CursedChestType.SINGLE;
    }

    @Override
    protected final void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AbstractChestBlock.CURSED_CHEST_TYPE);
        builder.add(AbstractChestBlock.PERP_ROTATION);
        builder.add(AbstractChestBlock.FACE_ROTATION);
        builder.add(AbstractChestBlock.Y_ROTATION);
        this.appendAdditionalStateDefinitions(builder);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        CursedChestType chestType = CursedChestType.SINGLE;
        Direction direction_1 = context.getHorizontalDirection().getOpposite();
        Direction direction_2 = context.getClickedFace();
        if (context.isSecondaryUseActive()) {
            BlockState state;
            if (direction_2.getAxis().isVertical()) {
                state = world.getBlockState(pos.relative(direction_2.getOpposite()));
                if (state.getBlock() == this && state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    Direction direction_3 = state.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y);
                    if (direction_3.getAxis() != direction_2.getAxis() && direction_3 == direction_1) {
                        chestType = direction_2 == Direction.UP ? CursedChestType.TOP : CursedChestType.BOTTOM;
                    }
                }
            } else {
                Direction offsetDir = direction_2.getOpposite();
                BlockState clickedBlock = world.getBlockState(pos.relative(offsetDir));
                if (clickedBlock.getBlock() == this && clickedBlock.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    if (clickedBlock.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y) == direction_2 && clickedBlock.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y) == direction_1) {
                        chestType = CursedChestType.FRONT;
                    } else {
                        state = world.getBlockState(pos.relative(direction_2.getOpposite()));
                        if (state.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y).get2DDataValue() < 2) {
                            offsetDir = offsetDir.getOpposite();
                        }
                        if (direction_1 == state.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y)) {
                            chestType = (offsetDir == Direction.WEST || offsetDir == Direction.NORTH) ? CursedChestType.LEFT : CursedChestType.RIGHT;
                        }
                    }
                }
            }
        } else {
            for (Direction dir : Direction.values()) {
                BlockState state = world.getBlockState(pos.relative(dir));
                if (state.getBlock() != this || state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != CursedChestType.SINGLE || state.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y) != direction_1) {
                    continue;
                }
                CursedChestType type = AbstractChestBlock.getChestType(FaceRotation.of(direction_1), state.getValue(AbstractChestBlock.FACE_ROTATION), state.getValue(AbstractChestBlock.PERP_ROTATION), dir);
                if (type != CursedChestType.SINGLE) {
                    chestType = type;
                    break;
                }
            }
        }
        return this.defaultBlockState().setValue(AbstractChestBlock.Y_ROTATION, FaceRotation.of(direction_1)).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, chestType);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction offset, BlockState offsetState, LevelAccessor world,
                                                BlockPos pos, BlockPos offsetPos) {
        DoubleBlockCombiner.BlockType mergeType = AbstractChestBlock.getBlockType(state);
        if (mergeType == DoubleBlockCombiner.BlockType.SINGLE) {
            Direction facing = state.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y);
            if (!offsetState.hasProperty(AbstractChestBlock.CURSED_CHEST_TYPE)) {
                return state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
            }
            CursedChestType newType = AbstractChestBlock.getChestType(FaceRotation.of(facing), state.getValue(AbstractChestBlock.FACE_ROTATION), state.getValue(AbstractChestBlock.PERP_ROTATION), offset);
            if (offsetState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == newType.getOpposite() && facing == offsetState.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y)) {
                return state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, newType);
            }
        } else if (world.getBlockState(pos.relative(AbstractChestBlock.getDirectionToAttached(state))).getBlock() != this) {
            return state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
        }
        return super.updateShape(state, offset, offsetState, world, pos, offsetPos);
    }

    protected void appendAdditionalStateDefinitions(StateDefinition.Builder<Block, BlockState> builder) {

    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(AbstractChestBlock.Y_ROTATION, state.getValue(AbstractChestBlock.Y_ROTATION).rotated(rotation));
    }

    public static <A extends AbstractOpenableStorageBlockEntity> PropertyRetriever<A> createPropertyRetriever(AbstractChestBlock<A> block, BlockState state, LevelAccessor world, BlockPos pos, boolean alwaysOpen) {
        //BiPredicate<WorldAccessor, BlockPos> isChestBlocked = alwaysOpen ? (_world, _pos) -> false : block::isAccessBlocked;
        //return DoubleBlockCombiner.combineWithNeigbour(block.getBlockEntityType(), AbstractChestBlock::getBlockType,
        //        AbstractChestBlock::getDirectionToAttached, AbstractChestBlock.Y_ROTATION, state, world, pos, isChestBlocked);
        // todo: reimplement
        return PropertyRetriever.create(world, state, pos);
    }

    protected abstract BlockEntityType<T> getBlockEntityType();

    protected boolean isAccessBlocked(LevelAccessor world, BlockPos pos) {
        return false;
    }

    @Override
    public OpenableBlockEntity getOpenableBlockEntity(Level world, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            //noinspection unchecked
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, world, pos, false).get(new Property<>() {
                @Override
                public OpenableBlockEntity get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
                    Component name = first.hasCustomTitle() ? first.getTitle() : second.hasCustomTitle() ? second.getTitle() : Utils.translation("container.expandedstorage.generic_double", first.getTitle());
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
    public WorldlyContainer getContainer(BlockState state, LevelAccessor world, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            //noinspection unchecked
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, world, pos, false).get(new Property<>() {
                @Override
                public WorldlyContainer get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
                    return VariableSidedInventory.of(first.getInventory(), second.getInventory());
                }

                @Override
                public WorldlyContainer get(AbstractOpenableStorageBlockEntity single) {
                    return single.getInventory();
                }
            });
        }
        return null;
    }
}