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
package ninjaphenix.expandedstorage.item;

import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.Utils;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner.BlockType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static net.minecraft.world.level.block.Rotation.CLOCKWISE_180;
import static net.minecraft.world.level.block.Rotation.CLOCKWISE_90;

public class StorageMutator extends Item {
    public StorageMutator(Item.Properties properties) {
        super(properties);
    }

    private static MutationMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("mode", Tag.TAG_BYTE)) {
            tag.putByte("mode", (byte) 0);
        }
        return MutationMode.from(tag.getByte("mode"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof BarrelBlock) {
            return this.useModifierOnBlock(context, state, pos, BlockType.SINGLE);
        } else if (block instanceof AbstractChestBlock) {
            return this.useModifierOnBlock(context, state, pos, AbstractChestBlock.getBlockType(state));
        } else {
            return this.useOnBlock(context, state, context.getClickedPos());
        }
    }

    protected InteractionResult useOnBlock(UseOnContext context, BlockState state, BlockPos pos) {
        ItemStack stack = context.getItemInHand();
        Level world = context.getLevel();
        Player player = context.getPlayer();
        Block block = state.getBlock();
        if (block instanceof net.minecraft.world.level.block.AbstractChestBlock) {
            if (StorageMutator.getMode(stack) == MutationMode.ROTATE) {
                if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
                    ChestType chestType = state.getValue(BlockStateProperties.CHEST_TYPE);
                    if (chestType != ChestType.SINGLE) {
                        if (!world.isClientSide()) {
                            BlockPos otherPos = pos.relative(net.minecraft.world.level.block.ChestBlock.getConnectedDirection(state));
                            BlockState otherState = world.getBlockState(otherPos);
                            world.setBlockAndUpdate(pos, state.rotate(CLOCKWISE_180).setValue(BlockStateProperties.CHEST_TYPE, state.getValue(BlockStateProperties.CHEST_TYPE).getOpposite()));
                            world.setBlockAndUpdate(otherPos, otherState.rotate(CLOCKWISE_180).setValue(BlockStateProperties.CHEST_TYPE, otherState.getValue(BlockStateProperties.CHEST_TYPE).getOpposite()));
                        }
                        //noinspection ConstantConditions
                        player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                        return InteractionResult.SUCCESS;
                    }
                }
                world.setBlockAndUpdate(pos, state.rotate(CLOCKWISE_90));
                //noinspection ConstantConditions
                player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                return InteractionResult.SUCCESS;
            }
        }
        if (block instanceof net.minecraft.world.level.block.ChestBlock) {
            MutationMode mode = StorageMutator.getMode(stack);
            if (mode == MutationMode.MERGE) {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("pos")) {
                    BlockPos otherPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
                    BlockState otherState = world.getBlockState(otherPos);
                    if (otherState.getBlock() == state.getBlock() &&
                            otherState.getValue(BlockStateProperties.HORIZONTAL_FACING) == state.getValue(BlockStateProperties.HORIZONTAL_FACING) &&
                            otherState.getValue(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE) {
                        if (!world.isClientSide()) {
                            BlockPos offset = otherPos.subtract(pos);
                            Direction direction = Direction.fromNormal(offset.getX(), offset.getY(), offset.getZ());
                            if (direction != null) {
                                CursedChestType type = ChestBlock.getChestType(state.getValue(BlockStateProperties.HORIZONTAL_FACING), direction);
                                Predicate<BlockEntity> isRandomizable = b -> b instanceof RandomizableContainerBlockEntity;
                                this.convertBlock(world, state, pos, Common.getTieredBlock(Common.CHEST_BLOCK_TYPE, Utils.WOOD_TIER.getId()), Utils.WOOD_STACK_COUNT, type, isRandomizable);
                                this.convertBlock(world, otherState, otherPos, Common.getTieredBlock(Common.CHEST_BLOCK_TYPE, Utils.WOOD_TIER.getId()), Utils.WOOD_STACK_COUNT, type.getOpposite(), isRandomizable);
                                tag.remove("pos");
                                //noinspection ConstantConditions
                                player.displayClientMessage(new TranslatableComponent("tooltip.expandedstorage.storage_mutator.merge_end"), true);
                            }
                        }
                        //noinspection ConstantConditions
                        player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    if (!world.isClientSide()) {
                        tag.put("pos", NbtUtils.writeBlockPos(pos));
                        //noinspection ConstantConditions
                        player.displayClientMessage(new TranslatableComponent("tooltip.expandedstorage.storage_mutator.merge_start"), true);
                    }
                    //noinspection ConstantConditions
                    player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                    return InteractionResult.SUCCESS;
                }
            } else if (mode == MutationMode.SPLIT) {
                ChestType chestType = state.getValue(BlockStateProperties.CHEST_TYPE);
                if (chestType != ChestType.SINGLE) {
                    if (!world.isClientSide()) {
                        BlockPos otherPos = pos.relative(net.minecraft.world.level.block.ChestBlock.getConnectedDirection(state));
                        BlockState otherState = world.getBlockState(otherPos);
                        world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE));
                        world.setBlockAndUpdate(otherPos, otherState.setValue(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE));
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (block instanceof net.minecraft.world.level.block.BarrelBlock) {
            if (StorageMutator.getMode(stack) == MutationMode.ROTATE) {
                if (!world.isClientSide()) {
                    Direction direction = state.getValue(FACING);
                    world.setBlockAndUpdate(pos, state.setValue(FACING, Direction.from3DDataValue(direction.get3DDataValue() + 1)));
                }
                //noinspection ConstantConditions
                player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    private void convertBlock(Level world, BlockState state, BlockPos pos, Block block, int slotCount, @Nullable CursedChestType type, Predicate<BlockEntity> check) {
        BlockEntity targetBlockEntity = world.getBlockEntity(pos);
        if (check.test(targetBlockEntity)) {
            NonNullList<ItemStack> invData = NonNullList.withSize(slotCount, ItemStack.EMPTY);
            //noinspection ConstantConditions
            ContainerHelper.loadAllItems(targetBlockEntity.save(new CompoundTag()), invData);
            world.removeBlockEntity(pos);
            BlockState newState = block.defaultBlockState();
            if (state.hasProperty(WATERLOGGED)) {
                newState = newState.setValue(WATERLOGGED, state.getValue(WATERLOGGED));
            }
            if (state.hasProperty(FACING)) {
                newState = newState.setValue(FACING, state.getValue(FACING));
            } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                newState = newState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
            }
            if (type != null) {
                newState = newState.setValue(ChestBlock.CURSED_CHEST_TYPE, type);
            }
            world.setBlockAndUpdate(pos, newState);
            BlockEntity newEntity = world.getBlockEntity(pos);
            //noinspection ConstantConditions
            newEntity.load(ContainerHelper.saveAllItems(newEntity.save(new CompoundTag()), invData));
        }
    }

    protected InteractionResult useModifierOnBlock(UseOnContext context, BlockState state, BlockPos pos, @SuppressWarnings("unused") BlockType type) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Block block = state.getBlock();
        MutationMode mode = StorageMutator.getMode(context.getItemInHand());
        if (mode == MutationMode.MERGE) {
            if (block instanceof AbstractChestBlock<?> chestBlock && state.getValue(ChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("pos")) {
                    BlockPos otherPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
                    BlockState otherState = world.getBlockState(otherPos);
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (block == otherState.getBlock()
                            && facing == otherState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                            && otherState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                        if (!world.isClientSide()) {
                            BlockPos offset = otherPos.subtract(pos);
                            Direction direction = Direction.fromNormal(offset.getX(), offset.getY(), offset.getZ());
                            if (direction != null) {
                                CursedChestType chestType = AbstractChestBlock.getChestType(state.getValue(BlockStateProperties.HORIZONTAL_FACING), direction);
                                Predicate<BlockEntity> isStorage = b -> b instanceof AbstractOpenableStorageBlockEntity;
                                this.convertBlock(world, state, pos, block, chestBlock.getSlotCount(), chestType, isStorage);
                                this.convertBlock(world, otherState, otherPos, block, chestBlock.getSlotCount(), chestType.getOpposite(), isStorage);
                                tag.remove("pos");
                                //noinspection ConstantConditions
                                player.displayClientMessage(new TranslatableComponent("tooltip.expandedstorage.storage_mutator.merge_end"), true);
                            }
                        }
                        //noinspection ConstantConditions
                        player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    if (!world.isClientSide()) {
                        tag.put("pos", NbtUtils.writeBlockPos(pos));
                        //noinspection ConstantConditions
                        player.displayClientMessage(new TranslatableComponent("tooltip.expandedstorage.storage_mutator.merge_start"), true);
                    }
                    //noinspection ConstantConditions
                    player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (mode == MutationMode.SPLIT) {
            if (block instanceof AbstractChestBlock && state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != CursedChestType.SINGLE) {
                if (!world.isClientSide()) {
                    BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                    BlockState otherState = world.getBlockState(otherPos);
                    world.setBlockAndUpdate(pos, state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE));
                    world.setBlockAndUpdate(otherPos, otherState.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE));
                }
                //noinspection ConstantConditions
                player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                return InteractionResult.SUCCESS;
            }
        } else if (mode == MutationMode.ROTATE) {
            if (state.hasProperty(FACING)) {
                if (!world.isClientSide()) {
                    Direction direction = state.getValue(FACING);
                    world.setBlockAndUpdate(pos, state.setValue(FACING, Direction.from3DDataValue(direction.get3DDataValue() + 1)));
                }
                //noinspection ConstantConditions
                player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                return InteractionResult.SUCCESS;
            } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                if (block instanceof AbstractChestBlock) {
                    if (!world.isClientSide()) {
                        CursedChestType value = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                        if (value == CursedChestType.SINGLE) {
                            world.setBlockAndUpdate(pos, state.rotate(CLOCKWISE_90));
                        } else if (value == CursedChestType.TOP || value == CursedChestType.BOTTOM) {
                            world.setBlockAndUpdate(pos, state.rotate(CLOCKWISE_90));
                            BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                            BlockState otherState = world.getBlockState(otherPos);
                            world.setBlockAndUpdate(otherPos, otherState.rotate(CLOCKWISE_90));
                        } else if (value == CursedChestType.FRONT || value == CursedChestType.BACK || value == CursedChestType.LEFT || value == CursedChestType.RIGHT) {
                            world.setBlockAndUpdate(pos, state.rotate(CLOCKWISE_180).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                            BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                            BlockState otherState = world.getBlockState(otherPos);
                            world.setBlockAndUpdate(otherPos, otherState.rotate(CLOCKWISE_180).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, otherState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                        }
                    }
                    //noinspection ConstantConditions
                    player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = this.useModifierInAir(world, player, hand);
        if (result.getResult() == InteractionResult.SUCCESS) {
            player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
        }
        return result;
    }

    private InteractionResultHolder<ItemStack> useModifierInAir(Level world, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            CompoundTag tag = stack.getOrCreateTag();
            MutationMode nextMode = StorageMutator.getMode(stack).next();
            tag.putByte("mode", nextMode.toByte());
            if (tag.contains("pos")) {
                tag.remove("pos");
            }
            if (!world.isClientSide()) {
                player.displayClientMessage(new TranslatableComponent("tooltip.expandedstorage.storage_mutator.description_" + nextMode, Utils.ALT_USE), true);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        super.onCraftedBy(stack, world, player);
        StorageMutator.getMode(stack);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        StorageMutator.getMode(stack);
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        if (this.allowdedIn(tab)) {
            stacks.add(this.getDefaultInstance());
        }
    }

    private MutableComponent getToolModeText(MutationMode mode) {
        return new TranslatableComponent("tooltip.expandedstorage.storage_mutator.tool_mode",
                new TranslatableComponent("tooltip.expandedstorage.storage_mutator." + mode));
    }

    @Override
    protected String getOrCreateDescriptionId() {
        return "item.expandedstorage.storage_mutator";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        MutationMode mode = StorageMutator.getMode(stack);
        list.add(this.getToolModeText(mode).withStyle(ChatFormatting.GRAY));
        list.add(Utils.translation("tooltip.expandedstorage.storage_mutator.description_" + mode, Utils.ALT_USE).withStyle(ChatFormatting.GRAY));
    }
}
