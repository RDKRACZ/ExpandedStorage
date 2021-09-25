package ninjaphenix.expandedstorage.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import ninjaphenix.container_library.api.helpers.VariableInventory;
import ninjaphenix.expandedstorage.block.ChestBlock;

public final class ChestBlockEntity extends AbstractChestBlockEntity {
    private final ChestLidController lidController;

    public ChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state, ((ChestBlock) state.getBlock()).getBlockId());
        lidController = new ChestLidController();
    }

    public static void progressLidAnimation(Level world, BlockPos pos, BlockState state, ChestBlockEntity blockEntity) {
        blockEntity.lidController.tickLid();
    }

    private static void playSound(Level world, BlockPos pos, BlockState state, SoundEvent sound) {
        DoubleBlockCombiner.BlockType mergeType = ChestBlock.getBlockType(state);
        Vec3 soundPos;
        if (mergeType == DoubleBlockCombiner.BlockType.SINGLE) {
            soundPos = Vec3.atCenterOf(pos);
        } else if (mergeType == DoubleBlockCombiner.BlockType.FIRST) {
            soundPos = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(ChestBlock.getDirectionToAttached(state).getNormal()).scale(0.5D));
        } else {
            return;
        }
        world.playSound(null, soundPos.x(), soundPos.y(), soundPos.z(), sound, SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected void onOpen(Level world, BlockPos pos, BlockState state) {
        ChestBlockEntity.playSound(world, pos, state, SoundEvents.CHEST_OPEN);
    }

    @Override
    protected void onClose(Level world, BlockPos pos, BlockState state) {
        ChestBlockEntity.playSound(world, pos, state, SoundEvents.CHEST_CLOSE);
    }

    @Override
    protected void onObserverCountChanged(Level world, BlockPos pos, BlockState state, int oldCount, int newCount) {
        world.blockEvent(pos, state.getBlock(), ChestBlock.SET_OBSERVER_COUNT_EVENT, newCount);
    }

    @Override
    protected boolean isThis(Container inventory) {
        return super.isThis(inventory) || inventory instanceof VariableInventory variableInventory && variableInventory.containsPart(this.getInventory());
    }

    @Override
    public boolean triggerEvent(int event, int value) {
        if (event == ChestBlock.SET_OBSERVER_COUNT_EVENT) {
            lidController.shouldBeOpen(value > 0);
            return true;
        }
        return super.triggerEvent(event, value);
    }

    // Client only
    public float getLidOpenness(float f) {
        return lidController.getOpenness(f);
    }
}
