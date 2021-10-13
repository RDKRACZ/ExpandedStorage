/**
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
package ninjaphenix.expandedstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import ninjaphenix.container_library.api.helpers.VariableSidedInventory;
import ninjaphenix.container_library.api.v2.OpenableBlockEntityV2;
import ninjaphenix.container_library.api.v2.helpers.OpenableBlockEntitiesV2;
import ninjaphenix.expandedstorage.Utils;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.block.misc.Property;
import ninjaphenix.expandedstorage.block.misc.PropertyRetriever;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

@Internal
@Experimental
public abstract class AbstractChestBlock<T extends AbstractOpenableStorageBlockEntity> extends AbstractOpenableStorageBlock implements InventoryProvider {
    public static final EnumProperty<CursedChestType> CURSED_CHEST_TYPE = EnumProperty.of("type", CursedChestType.class);

    public AbstractChestBlock(Settings properties, Identifier blockId, Identifier blockTier, Identifier openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
        this.setDefaultState(this.getStateManager().getDefaultState()
                                 .with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE)
                                 .with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    public static Direction getDirectionToAttached(BlockState state) {
        CursedChestType value = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (value == CursedChestType.TOP) {
            return Direction.DOWN;
        } else if (value == CursedChestType.BACK) {
            return state.get(Properties.HORIZONTAL_FACING);
        } else if (value == CursedChestType.RIGHT) {
            return state.get(Properties.HORIZONTAL_FACING).rotateYClockwise();
        } else if (value == CursedChestType.BOTTOM) {
            return Direction.UP;
        } else if (value == CursedChestType.FRONT) {
            return state.get(Properties.HORIZONTAL_FACING).getOpposite();
        } else if (value == CursedChestType.LEFT) {
            return state.get(Properties.HORIZONTAL_FACING).rotateYCounterclockwise();
        } else if (value == CursedChestType.SINGLE) {
            throw new IllegalArgumentException("BaseChestBlock#getDirectionToAttached received an unexpected state.");
        }
        throw new IllegalArgumentException("Invalid CursedChestType passed.");
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

    public static CursedChestType getChestType(Direction facing, Direction offset) {
        if (facing.rotateYClockwise() == offset) {
            return CursedChestType.RIGHT;
        } else if (facing.rotateYCounterclockwise() == offset) {
            return CursedChestType.LEFT;
        } else if (facing == offset) {
            return CursedChestType.BACK;
        } else if (facing == offset.getOpposite()) {
            return CursedChestType.FRONT;
        } else if (offset == Direction.DOWN) {
            return CursedChestType.TOP;
        } else if (offset == Direction.UP) {
            return CursedChestType.BOTTOM;
        }
        return CursedChestType.SINGLE;
    }

    @Override
    protected final void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AbstractChestBlock.CURSED_CHEST_TYPE);
        builder.add(Properties.HORIZONTAL_FACING);
        this.appendAdditionalStateDefinitions(builder);
    }

    @NotNull
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        CursedChestType chestType = CursedChestType.SINGLE;
        Direction direction_1 = context.getPlayerFacing().getOpposite();
        Direction direction_2 = context.getSide();
        if (context.shouldCancelInteraction()) {
            BlockState state;
            if (direction_2.getAxis().isVertical()) {
                state = world.getBlockState(pos.offset(direction_2.getOpposite()));
                if (state.getBlock() == this && state.get(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    Direction direction_3 = state.get(Properties.HORIZONTAL_FACING);
                    if (direction_3.getAxis() != direction_2.getAxis() && direction_3 == direction_1) {
                        chestType = direction_2 == Direction.UP ? CursedChestType.TOP : CursedChestType.BOTTOM;
                    }
                }
            } else {
                Direction offsetDir = direction_2.getOpposite();
                BlockState clickedBlock = world.getBlockState(pos.offset(offsetDir));
                if (clickedBlock.getBlock() == this && clickedBlock.get(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    if (clickedBlock.get(Properties.HORIZONTAL_FACING) == direction_2 && clickedBlock.get(Properties.HORIZONTAL_FACING) == direction_1) {
                        chestType = CursedChestType.FRONT;
                    } else {
                        state = world.getBlockState(pos.offset(direction_2.getOpposite()));
                        if (state.get(Properties.HORIZONTAL_FACING).getHorizontal() < 2) {
                            offsetDir = offsetDir.getOpposite();
                        }
                        if (direction_1 == state.get(Properties.HORIZONTAL_FACING)) {
                            chestType = (offsetDir == Direction.WEST || offsetDir == Direction.NORTH) ? CursedChestType.LEFT : CursedChestType.RIGHT;
                        }
                    }
                }
            }
        } else {
            for (Direction dir : Direction.values()) {
                BlockState state = world.getBlockState(pos.offset(dir));
                if (state.getBlock() != this || state.get(AbstractChestBlock.CURSED_CHEST_TYPE) != CursedChestType.SINGLE || state.get(Properties.HORIZONTAL_FACING) != direction_1) {
                    continue;
                }
                CursedChestType type = getChestType(direction_1, dir);
                if (type != CursedChestType.SINGLE) {
                    chestType = type;
                    break;
                }
            }
        }
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, direction_1).with(AbstractChestBlock.CURSED_CHEST_TYPE, chestType);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction offset, BlockState offsetState, WorldAccess world,
                                                BlockPos pos, BlockPos offsetPos) {
        DoubleBlockProperties.Type mergeType = getBlockType(state);
        if (mergeType == DoubleBlockProperties.Type.SINGLE) {
            Direction facing = state.get(Properties.HORIZONTAL_FACING);
            if (!offsetState.contains(AbstractChestBlock.CURSED_CHEST_TYPE)) {
                return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
            }
            CursedChestType newType = getChestType(facing, offset);
            if (offsetState.get(AbstractChestBlock.CURSED_CHEST_TYPE) == newType.getOpposite() && facing == offsetState.get(Properties.HORIZONTAL_FACING)) {
                return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, newType);
            }
        } else {
            BlockState otherState = world.getBlockState(pos.offset(AbstractChestBlock.getDirectionToAttached(state)));
            if (otherState.getBlock() != this ||
                    otherState.get(CURSED_CHEST_TYPE) != state.get(CURSED_CHEST_TYPE).getOpposite() ||
                    state.get(Properties.HORIZONTAL_FACING) != state.get(Properties.HORIZONTAL_FACING)) {
                return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
            }
        }
        return super.getStateForNeighborUpdate(state, offset, offsetState, world, pos, offsetPos);
    }

    protected void appendAdditionalStateDefinitions(StateManager.Builder<Block, BlockState> builder) {

    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
    }

    public static <A extends AbstractOpenableStorageBlockEntity> PropertyRetriever<A> createPropertyRetriever(AbstractChestBlock<A> block, BlockState state, WorldAccess world, BlockPos pos, boolean alwaysOpen) {
        BiPredicate<WorldAccess, BlockPos> isChestBlocked = alwaysOpen ? (_level, _pos) -> false : block::isAccessBlocked;
        return PropertyRetriever.create(block.getBlockEntityType(), AbstractChestBlock::getBlockType, AbstractChestBlock::getDirectionToAttached,
                (s) -> s.get(Properties.HORIZONTAL_FACING), state, world, pos, isChestBlocked);
    }

    protected abstract BlockEntityType<T> getBlockEntityType();

    protected boolean isAccessBlocked(WorldAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public OpenableBlockEntityV2 getOpenableBlockEntity(World world, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            //noinspection unchecked
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, world, pos, false).get(new Property<AbstractOpenableStorageBlockEntity, OpenableBlockEntityV2>() {
                @Override
                public OpenableBlockEntityV2 get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
                    Text name = first.hasCustomTitle() ? first.getTitle() : second.hasCustomTitle() ? second.getTitle() : Utils.translation("container.expandedstorage.generic_double", first.getTitle());
                    return new OpenableBlockEntitiesV2(name, first, second);
                }

                @Override
                public OpenableBlockEntityV2 get(AbstractOpenableStorageBlockEntity single) {
                    return single;
                }
            }).orElse(null);
        }
        return null;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            //noinspection unchecked
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, world, pos, false).get(new Property<AbstractOpenableStorageBlockEntity, SidedInventory>() {
                @Override
                public SidedInventory get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
                    return VariableSidedInventory.of(first.getInventory(), second.getInventory());
                }

                @Override
                public SidedInventory get(AbstractOpenableStorageBlockEntity single) {
                    return single.getInventory();
                }
            }).orElse(null);
        }
        return null;
    }
}
