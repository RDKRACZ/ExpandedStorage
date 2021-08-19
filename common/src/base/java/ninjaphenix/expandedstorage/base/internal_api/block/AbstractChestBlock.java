package ninjaphenix.expandedstorage.base.internal_api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.FaceRotation;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.Property;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.PropertyRetriever;
import ninjaphenix.expandedstorage.base.internal_api.inventory.SyncedMenuFactory;
import ninjaphenix.expandedstorage.base.wrappers.NetworkWrapper;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Internal
@Experimental
public abstract class AbstractChestBlock<T extends AbstractOpenableStorageBlockEntity> extends AbstractOpenableStorageBlock {
    public static final EnumProperty<CursedChestType> CURSED_CHEST_TYPE = EnumProperty.create("type", CursedChestType.class);
    public static final EnumProperty<FaceRotation> FACE_ROTATION = EnumProperty.create("face_rotation", FaceRotation.class);
    public static final EnumProperty<FaceRotation> PERP_ROTATION = EnumProperty.create("perp_rotation", FaceRotation.class);
    public static final EnumProperty<FaceRotation> Y_ROTATION = DirectionProperty.create("facing", FaceRotation.class);

    private final Property<T, SyncedMenuFactory> menuProperty = new Property<>() {
        @Override
        public SyncedMenuFactory get(T first, T second) {
            return new SyncedMenuFactory() {
                @Override
                public void writeClientData(ServerPlayer player, FriendlyByteBuf buffer) {
                    buffer.writeBlockPos(first.getBlockPos()).writeInt(first.getSlotCount() + second.getSlotCount());
                }

                @Override
                public Component getMenuTitle() {
                    return first.hasCustomName() ? first.getName() : second.hasCustomName() ? second.getName() : Utils.translation("container.expandedstorage.generic_double", first.getName());
                }

                @Override
                public boolean canPlayerOpen(ServerPlayer player) {
                    if (first.canPlayerInteractWith(player) && second.canPlayerInteractWith(player)) {
                        return true;
                    }
                    AbstractStorageBlockEntity.notifyBlockLocked(player, this.getMenuTitle());
                    return false;
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, ServerPlayer player) {
                    if (first.canContinueUse(player) && second.canContinueUse(player)) {
                        CompoundContainer container = new CompoundContainer(first.getContainerWrapper(), second.getContainerWrapper());
                        return NetworkWrapper.getInstance().createMenu(windowId, first.getBlockPos(), container, playerInventory, this.getMenuTitle());
                    }
                    return null;
                }
            };
        }

        @Override
        public SyncedMenuFactory get(T single) {
            return new SyncedMenuFactory() {
                @Override
                public void writeClientData(ServerPlayer player, FriendlyByteBuf buffer) {
                    buffer.writeBlockPos(single.getBlockPos()).writeInt(single.getSlotCount());
                }

                @Override
                public Component getMenuTitle() {
                    return single.getName();
                }

                @Override
                public boolean canPlayerOpen(ServerPlayer player) {
                    if (single.canPlayerInteractWith(player)) {
                        return true;
                    }
                    AbstractStorageBlockEntity.notifyBlockLocked(player, this.getMenuTitle());
                    return false;
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, ServerPlayer player) {
                    if (single.canContinueUse(player)) {
                        return NetworkWrapper.getInstance().createMenu(windowId, single.getBlockPos(), single.getContainerWrapper(), playerInventory, this.getMenuTitle());
                    }
                    return null;
                }
            };
        }
    };

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
        var definitiveFacing = yRotation.asDirection(Direction.Axis.Y);
        var definitiveOffset = FaceRotation.getDefinitiveDirection(offset, faceRotation, yRotation, perpRotation);
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
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        CursedChestType chestType = CursedChestType.SINGLE;
        Direction direction_1 = context.getHorizontalDirection().getOpposite();
        Direction direction_2 = context.getClickedFace();
        if (context.isSecondaryUseActive()) {
            BlockState state;
            if (direction_2.getAxis().isVertical()) {
                state = level.getBlockState(pos.relative(direction_2.getOpposite()));
                if (state.getBlock() == this && state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    Direction direction_3 = state.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y);
                    if (direction_3.getAxis() != direction_2.getAxis() && direction_3 == direction_1) {
                        chestType = direction_2 == Direction.UP ? CursedChestType.TOP : CursedChestType.BOTTOM;
                    }
                }
            } else {
                Direction offsetDir = direction_2.getOpposite();
                BlockState clickedBlock = level.getBlockState(pos.relative(offsetDir));
                if (clickedBlock.getBlock() == this && clickedBlock.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    if (clickedBlock.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y) == direction_2 && clickedBlock.getValue(AbstractChestBlock.Y_ROTATION).asDirection(Direction.Axis.Y) == direction_1) {
                        chestType = CursedChestType.FRONT;
                    } else {
                        state = level.getBlockState(pos.relative(direction_2.getOpposite()));
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
                BlockState state = level.getBlockState(pos.relative(dir));
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
    public BlockState updateShape(BlockState state, Direction offset, BlockState offsetState, LevelAccessor level,
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
        } else if (level.getBlockState(pos.relative(AbstractChestBlock.getDirectionToAttached(state))).getBlock() != this) {
            return state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
        }
        return super.updateShape(state, offset, offsetState, level, pos, offsetPos);
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

    public static <A extends AbstractOpenableStorageBlockEntity> PropertyRetriever<A> createPropertyRetriever(AbstractChestBlock<A> block, BlockState state, LevelAccessor level, BlockPos pos, boolean alwaysOpen) {
        //BiPredicate<LevelAccessor, BlockPos> isChestBlocked = alwaysOpen ? (_level, _pos) -> false : block::isAccessBlocked;
        //return DoubleBlockCombiner.combineWithNeigbour(block.getBlockEntityType(), AbstractChestBlock::getBlockType,
        //        AbstractChestBlock::getDirectionToAttached, AbstractChestBlock.Y_ROTATION, state, level, pos, isChestBlocked);
        // todo: reimplement
        return PropertyRetriever.create();
    }

    protected abstract BlockEntityType<T> getBlockEntityType();

    protected boolean isAccessBlocked(LevelAccessor level, BlockPos pos) {
        return false;
    }

    @Nullable
    @Override
    protected SyncedMenuFactory createMenuFactory(BlockState state, LevelAccessor level, BlockPos pos) {
        return AbstractChestBlock.createPropertyRetriever(this, state, level, pos, false).get(menuProperty);
    }
}
